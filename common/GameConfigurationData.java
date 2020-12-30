package com.han.startup.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.ubisoft.hfx.mm.enumeration.MatchmakingTimeOutBehaviour;
import com.ubisoft.hfx.mm.enumeration.PlatformType;
import lombok.*;

import java.util.List;

import static com.ubisoft.hfx.mm.enumeration.MatchmakingTimeOutBehaviour.PROCEED_WITH_AI;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameConfigurationData {
    //Session
    @Builder.Default
    boolean reconnectable = true;
    @Builder.Default
    List<ServiceAccessKeyEncryptionToken> userSessionEncPws = Lists.newArrayList();
    @JsonProperty(value = "csm_init_expr")
    @Builder.Default
    int codeShareMatchInitialExpirySeconds = 10;
    @JsonProperty(value = "csm_ext_amt")
    @Builder.Default
    int codeShareMatchExtendAmountSeconds = 10;
    @JsonProperty(value = "csm_auto_start")
    @Builder.Default
    boolean autoStartMatchWhenAllReady = false;
    @JsonProperty(value = "scm_expr")
    @Builder.Default
    long codeShareMatchCodeExpirySeconds = 3600;
    @JsonProperty(value = "scm_evt_to")
    @Builder.Default
    int codeShareMatchEventPollingTimeoutSeconds = 3;
    @Builder.Default
    String playerMatchMakingEventPubSubKey = "p_mm_evt";
    @JsonProperty(value = "csm_evt_key")
    @Builder.Default
    String csmEventPubSubKey = "csm_evt";
    @JsonProperty(value = "us_expr")
    @Builder.Default
    int userStatusKeyExpireInSeconds = 7;
    @JsonProperty(value = "kfk_platvt_pt_sz")
    @Builder.Default
    int kafkaPlayerActivityDefaultPartitionSize = 100;
    @JsonProperty(value = "kfk_platvt_rpf")
    @Builder.Default
    short kafkaPlayerActivityDefaultReplicationFactor = 2;

    @JsonProperty(value = "ss_usr_grp_expr")
    @Builder.Default
    int userGroupParticipationCacheExpirySeconds = 30;
    @JsonProperty(value = "ss_usr_hb_intv")
    @Builder.Default
    int userHeartBeatIntervalSeconds = 5;
    @JsonProperty(value = "ss_grp_evt_key")
    @Builder.Default
    String groupEventPubSubKey = "grp_evt";
    @JsonProperty(value = "ss_grp_usr_evt_key")
    @Builder.Default
    String groupUserEventPubSubKey = "grp_usr_evt";


    //MM GW
    @JsonProperty(value = "kfk_plevt_pt_sz")
    @Builder.Default
    int kafkaPlayerEventDefaultPartitionSize = 100;
    @JsonProperty(value = "kfk_plevt_rpf")
    @Builder.Default
    short kafkaPlayerEventDefaultReplicationFactor = 2;


    //MM
    @Builder.Default
    double randomMatchRatio = 0;
    @Builder.Default
    double rankedMatchRatio = 10;
    @Builder.Default
    double friendMatchRatio = 0;
    @Builder.Default
    double leagueRatio = 0;
    @Builder.Default
    int matchmakingTimeOutSeconds = 20;
    @Builder.Default
    int clientMatchMakingTimeOutSeconds = 30;
    @Builder.Default
    MatchmakingTimeOutBehaviour matchmakingTimeOutBehaviour = PROCEED_WITH_AI;
    @Builder.Default
    int minMatchMakingRequestInterval = 5;
    @Builder.Default
    int maxEntriesMmWtEval = 100;
    @Builder.Default
    int maxRedisEntriesMmWtEval = 10;
    @JsonProperty(value = "mm_ins_rds_chk")
    @Builder.Default
    boolean checkRedisBeforeAllocation = false;
    @JsonProperty(value = "mm_ins_zk_chk")
    @Builder.Default
    boolean checkZookeeperBeforeAllocation = true;
    @JsonProperty(value = "mm_strt_dly")
    @Builder.Default
    int matchStartDelaySeconds = 10;
    @JsonProperty(value = "mm_ins_prcs")
    @Builder.Default
    int instanceStatePrecision = 5;
    @JsonProperty(value = "mm_rt_lmt")
    @Builder.Default
    int requestSubscriptionRateLimit = 200;
    @JsonProperty(value = "mm_req_q_lmt")
    @Builder.Default
    int mmRequestWaitingQueueThreshold = 200;
    @JsonProperty(value = "mm_def_spc")
    @Builder.Default
            //FIXME uat/crossplay temp
            String defaultSpaceId = "d1947c7d-cdd6-4e67-b8c8-e4030f56821d";
    @JsonProperty(value = "mm_def_env")
    @Builder.Default
    //FIXME uat/crossplay temp
            String defaultEnvironment = "uat";

    //ORCHESTRATION
    @Builder.Default
    List<ServiceAccessKeyEncryptionToken> gameInstanceEncPws = Lists.newArrayList();
    @Builder.Default
    boolean checkAccessKey = true;
    @Builder.Default
    int minReservedGameInstances = 1;
    @Builder.Default
    String gameInstanceMatchMakingEventPubSubKey = "gi_mm_evt";
    @Builder.Default
    boolean removeInstanceFromRedis = true;
    @JsonProperty(value = "g_st_d")
    @Builder.Default
    long gameStartDelay = 1000;
    @JsonProperty(value = "kfk_gi_pt_sz")
    @Builder.Default
    int kafkaGameInstanceSupplyDefaultPartitionSize = 100;
    @JsonProperty(value = "kfk_gi_rpf")
    @Builder.Default
    short kafkaGameInstanceDefaultReplicationFactor = 2;

    //Common
    @JsonProperty(value = "ts_metric")
    @Builder.Default
    boolean collectTsMetrics = true;
    @JsonProperty(value = "kafka_enabled")
    @Builder.Default
    boolean kafkaEnabled = true;

    //MM-QC
    boolean sendTestMessage = false;

    @Builder.Default
    List<PlatformAvailability> platforms = Lists.newArrayList(
            PlatformAvailability.builder().platformType(PlatformType.AWS_ONDEMAND).enabled(true).build(),
            PlatformAvailability.builder().platformType(PlatformType.I3D_BAREMETAL).enabled(false).build());

    @Builder.Default
    List<PlatformConsumePriority> consumePriorities = Lists.newArrayList(PlatformConsumePriority.builder().platformType(PlatformType.AWS_ONDEMAND).priority(0).build(),
            PlatformConsumePriority.builder().platformType(PlatformType.I3D_BAREMETAL).priority(10).build());

}
