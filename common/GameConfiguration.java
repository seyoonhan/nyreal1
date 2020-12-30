package com.han.startup.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.ubisoft.hfx.mm.enumeration.MatchType;
import com.ubisoft.hfx.mm.model.GameInstance;
import com.ubisoft.hfx.mm.model.HostInstance;
import com.ubisoft.hfx.support.util.WeightedRandomPackage;
import com.ubisoft.hfx.zk.ZkConnectionManager;
import com.ubisoft.hfx.zk.listener.ZkNodeGameConfigurationEventListener;
import com.ubisoft.hfx.zk.listener.ZkNodeGameInstanceEventListener;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;

@Data
@Component
@Slf4j
public class GameConfiguration {
    @Getter
    GameConfigurationData data;

    Map<MatchType, String> gameInstanceGroupZkPathPerMatchTypes;
    Map<MatchType, String> hostInstanceGroupZkPathPerMatchTypes;
    WeightedRandomPackage randomPackage = null;

    @Value("${giZkRootPath ?:/giroot}")
    String gameInstanceZkRootPath;

    @Value("${hiZkRootPath ?:/giroot}")
    String hostInstanceZkRootPath;

    @Value("${giConfZkPath ?:/giconf}")
    String configurationPath;

    @Value("${host.key.template}")
    public String HOST_KEY_TEMPLATE = "hi_%s";

    @Value("${game.instance.key.template}")
    public String GAME_INSTANCE_KEY_TEMPLATE = "gi_%s";

    @Value("${server.port:10080}")
    public int SERVER_PORT;

    @Value("${private.ip:127.0.0.1}")
    public String PRIVATE_IP;

    @Value("${external.instance.id:default}")
    public String EXTERNAL_INSTANCE_ID;

    @Value("${external.cluster.id:default}")
    public String EXTERNAL_CLUSTER_ID;

    public static String CODE_SHARE_MATCH_KEY_PREFIX = "cd_mm_";
    public static String CODE_SHARE_MATCH_CODE_TO_OWNER_REF_PREFIX = "cd_mm_o_";
    public static String CODE_SHARE_MATCH_STATUS_KEY_PREFIX= "cd_st_";
    public static int MAX_PLAYERS_PER_MATCH = 10;

    public static String USER_STATUS_KEY_PREFIX="us_";

    List<ZkNodeGameInstanceEventListener> gameInstanceZkNodeGameInstanceEventListeners;

    List<ZkNodeGameConfigurationEventListener> gameConfigurationZkNodeGameInstanceEventListener;

    @PostConstruct
    void prepare() {
        gameInstanceGroupZkPathPerMatchTypes = Maps.newHashMap();
        gameInstanceGroupZkPathPerMatchTypes.put(MatchType.RANDOM, gameInstanceZkRootPath + "/" + MatchType.RANDOM.name());
        gameInstanceGroupZkPathPerMatchTypes.put(MatchType.RANKED, gameInstanceZkRootPath + "/" + MatchType.RANKED.name());
        gameInstanceGroupZkPathPerMatchTypes.put(MatchType.FRIEND, gameInstanceZkRootPath + "/" + MatchType.FRIEND.name());
        gameInstanceGroupZkPathPerMatchTypes.put(MatchType.LEAGUE, gameInstanceZkRootPath + "/" + MatchType.LEAGUE.name());

        hostInstanceGroupZkPathPerMatchTypes = Maps.newHashMap();
        hostInstanceGroupZkPathPerMatchTypes.put(MatchType.RANDOM, hostInstanceZkRootPath + "/" + MatchType.RANDOM.name());
        hostInstanceGroupZkPathPerMatchTypes.put(MatchType.RANKED, hostInstanceZkRootPath + "/" + MatchType.RANKED.name());
        hostInstanceGroupZkPathPerMatchTypes.put(MatchType.FRIEND, hostInstanceZkRootPath + "/" + MatchType.FRIEND.name());
        hostInstanceGroupZkPathPerMatchTypes.put(MatchType.LEAGUE, hostInstanceZkRootPath + "/" + MatchType.LEAGUE.name());

        data = GameConfigurationData.builder().build();
        try {
            readConfigurationFromZk();
        } catch (Exception e) {
            log.error("Failed to read configuration from zookeeper: " + e.getMessage(), e);
        }

        initializeConfigurationChangeWatcher();
        initializeHostInstanceUpdateWatcher();
        initializeGameInstanceUpdateWatcher();

        try {
            configurationNodeTreeCache.start();
            hostNodeTreeCache.start();
            gameInstanceNodeTreeCache.start();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        gameInstanceZkNodeGameInstanceEventListeners = Lists.newArrayList();
        gameConfigurationZkNodeGameInstanceEventListener = Lists.newArrayList();
    }

    @Autowired
    ZkConnectionManager zkConnectionManager;

    @Autowired
    ObjectMapper objectMapper;

    @Getter
    TreeCache configurationNodeTreeCache;

    @Getter
    TreeCache hostNodeTreeCache;

    @Getter
    TreeCache gameInstanceNodeTreeCache;


    public String createGameInstanceRedisKey(String ip) {
        return String.format(GAME_INSTANCE_KEY_TEMPLATE, ip);
    }

    public String getZkPathForGameInstance(MatchType matchType) {
        return gameInstanceGroupZkPathPerMatchTypes.get(matchType);
    }

    public String getZkPathForHostInstance(MatchType matchType) {
        return hostInstanceGroupZkPathPerMatchTypes.get(matchType);
    }

    public void addGameInstanceZkEventListener(ZkNodeGameInstanceEventListener eventListener) {
        gameInstanceZkNodeGameInstanceEventListeners.add(eventListener);
    }

    public void addGameConfigurationZkEventListener(ZkNodeGameConfigurationEventListener eventListener) {
        gameConfigurationZkNodeGameInstanceEventListener.add(eventListener);
    }

    public WeightedRandomPackage<MatchType> getWeighedRandomPackage() {
        if (randomPackage == null) {
            randomPackage = new WeightedRandomPackage();
        }

        if (data.getRandomMatchRatio() > 0) {
            randomPackage.addEntry(MatchType.RANDOM, data.getRandomMatchRatio());
        }

        if (data.getRankedMatchRatio() > 0) {
            randomPackage.addEntry(MatchType.RANKED, data.getRankedMatchRatio());
        }

        if (data.getFriendMatchRatio() > 0) {
            randomPackage.addEntry(MatchType.FRIEND, data.getFriendMatchRatio());
        }

        if (data.getLeagueRatio() > 0) {
            randomPackage.addEntry(MatchType.LEAGUE, data.getLeagueRatio());
        }

        return randomPackage;
    }

    public void readConfigurationFromZk() throws Exception {
        CuratorFramework zkClient = zkConnectionManager.getZkClient();
        Stat pathStat = zkClient.checkExists().forPath(configurationPath);

        if (pathStat == null) {
            if (data == null) {
                data = GameConfigurationData.builder().build();
            }

            writeData(zkClient, data, true);
            return;
        } else {
            byte[] bytes = zkClient.getData().forPath(configurationPath);
            if (bytes == null || bytes.length < 1) {
                if (data == null) {
                    data = GameConfigurationData.builder().build();
                }

                byte[] contentByteArray = objectMapper.writeValueAsBytes(data);
                zkClient.setData().forPath(configurationPath, contentByteArray);
            } else {
                data = objectMapper.readValue(bytes, GameConfigurationData.class);
            }
        }
    }

    public void writeData(CuratorFramework zkClient, GameConfigurationData data, boolean createPath) throws Exception {
        byte[] contentByteArray = objectMapper.writeValueAsBytes(data);
        if (createPath) {
            zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT)
                    .forPath(configurationPath, contentByteArray);
        } else {
            zkClient.setData().forPath(configurationPath, contentByteArray);
        }
    }

    protected void initializeConfigurationChangeWatcher() {
        CuratorFramework zkClient = zkConnectionManager.getZkClient();
        configurationNodeTreeCache = new TreeCache(zkClient, configurationPath);
        configurationNodeTreeCache.getListenable().addListener((client, event) -> {
            if (event.getData() != null && event.getData().getData() != null && event.getData().getData().length > 0) {
                String eventPath = event.getData().getPath();
                GameConfigurationData updated = objectMapper.readValue(event.getData().getData(), GameConfigurationData.class);
                switch (event.getType()) {
                    case CONNECTION_LOST:
                    case CONNECTION_SUSPENDED:
                    case NODE_REMOVED:
                        log.error(MessageFormat.format("GameConfiguration - {0}, {1}", event.getType().name(), objectMapper.writeValueAsString(event)));
                        break;
                    case CONNECTION_RECONNECTED:
                    case INITIALIZED:
                    case NODE_ADDED:
                        log.info(MessageFormat.format("GameConfiguration - {0}, {1}", event.getType().name(), objectMapper.writeValueAsString(event)));
                        break;
                    case NODE_UPDATED:
                        log.info(MessageFormat.format("GameConfiguration - {0}, {1}", event.getType().name(), objectMapper.writeValueAsString(event)));
                        this.data = updated;
                        break;
                }

                if (gameInstanceZkNodeGameInstanceEventListeners.size() > 0) {
                    for (ZkNodeGameConfigurationEventListener zkNodeGameInstanceEventListener : gameConfigurationZkNodeGameInstanceEventListener) {
                        zkNodeGameInstanceEventListener.onEvent(event, eventPath, updated);
                    }
                }
            }
        });
    }

    @SuppressWarnings("Duplicates")
    protected void initializeGameInstanceUpdateWatcher() {
        CuratorFramework zkClient = zkConnectionManager.getZkClient();
        //TODO support other types
        String zkPathForGameInstance = getZkPathForGameInstance(MatchType.RANKED);
        gameInstanceNodeTreeCache = new TreeCache(zkClient, zkPathForGameInstance);
        gameInstanceNodeTreeCache.getListenable().addListener((client, event) -> {
            GameInstance gameInstance;
            if (event.getData() != null && event.getData().getData() != null && event.getData().getData().length > 0) {
                String eventPath = event.getData().getPath();
                for (MatchType matchType : gameInstanceGroupZkPathPerMatchTypes.keySet()) {
                    if (eventPath.contains(gameInstanceGroupZkPathPerMatchTypes.get(matchType))) {
                        gameInstance = objectMapper.readValue(event.getData().getData(), GameInstance.class);
                        if (gameInstanceZkNodeGameInstanceEventListeners.size() > 0) {
                            for (ZkNodeGameInstanceEventListener zkNodeGameInstanceEventListener : gameInstanceZkNodeGameInstanceEventListeners) {
                                zkNodeGameInstanceEventListener.onEvent(event, eventPath, gameInstance);
                            }
                        }
                    }
                }
            }
        });
    }

    @SuppressWarnings("Duplicates")
    protected void initializeHostInstanceUpdateWatcher() {
        CuratorFramework zkClient = zkConnectionManager.getZkClient();
        hostNodeTreeCache = new TreeCache(zkClient, getZkPathForHostInstance(MatchType.RANKED));
        hostNodeTreeCache.getListenable().addListener((client, event) -> {
            if (event.getData() != null && event.getData().getData() != null && event.getData().getData().length > 0) {
                String eventPath = event.getData().getPath();
                for (MatchType matchType : hostInstanceGroupZkPathPerMatchTypes.keySet()) {
                    if (hostInstanceGroupZkPathPerMatchTypes.get(matchType).contains(eventPath)) {
                        HostInstance updated = objectMapper.readValue(event.getData().getData(), HostInstance.class);
                        switch (event.getType()) {
                            case CONNECTION_LOST:
                            case CONNECTION_SUSPENDED:
                            case NODE_REMOVED:
                                log.error("[ZKCONN] " + event.getType().name() + ", " + objectMapper.writeValueAsString(event));
                                break;
                            case CONNECTION_RECONNECTED:
                            case INITIALIZED:
                                log.info("[ZKCONN] " + event.getType().name() + ", " + objectMapper.writeValueAsString(event));
                                break;
                            case NODE_ADDED:
                                log.info("[ZKCONN] " + event.getType().name() + ", " + objectMapper.writeValueAsString(event));

                                //todo add node to pool

                                break;
                            case NODE_UPDATED:
                                log.info("[ZKCONN] " + event.getType().name() + ", " + objectMapper.writeValueAsString(event));
                                //TODO check what has been changed
                                break;
                        }
                    }
                }
            }
        });
    }
}
