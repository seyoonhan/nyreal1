package com.han.startup.configuration;

import com.ubisoft.hfx.common.GameConfiguration;
import com.ubisoft.hfx.mm.enumeration.PlatformType;
import com.ubisoft.hfx.mm.logging.MatchMakingKafkaLogging;
import com.ubisoft.hfx.mm.pipeline.GameInstancePublisher;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaPredefinedTopics {

    @Autowired
    GameConfiguration gameConfiguration;

    @Bean
    public NewTopic playerEvent() {
        return new NewTopic(MatchMakingKafkaLogging.PLAYER_EVENT_TOPIC,
                gameConfiguration.getData().getKafkaPlayerEventDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaPlayerEventDefaultReplicationFactor());
    }

    @Bean
    public NewTopic playerActivity() {
        return new NewTopic(MatchMakingKafkaLogging.PLAYER_ACTIVITY_TOPIC,
                gameConfiguration.getData().getKafkaPlayerActivityDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaPlayerActivityDefaultReplicationFactor());
    }

    @Bean
    public NewTopic defaultGameInstanceQueue() {
        return new NewTopic(GameInstancePublisher.GAME_INSTANCE_SUPPLY_BUFFER,
                gameConfiguration.getData().getKafkaGameInstanceSupplyDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaGameInstanceDefaultReplicationFactor());
    }

    @Bean
    public NewTopic awsOnDemandGameInstanceQueue() {
        return new NewTopic(GameInstancePublisher.getDataKeyForPlatform(PlatformType.AWS_ONDEMAND),
                gameConfiguration.getData().getKafkaGameInstanceSupplyDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaGameInstanceDefaultReplicationFactor());
    }

    @Bean
    public NewTopic awsReservedGameInstanceQueue() {
        return new NewTopic(GameInstancePublisher.getDataKeyForPlatform(PlatformType.AWS_RI),
                gameConfiguration.getData().getKafkaGameInstanceSupplyDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaGameInstanceDefaultReplicationFactor());
    }

    @Bean
    public NewTopic i3dGameInstanceQueue() {
        return new NewTopic(GameInstancePublisher.getDataKeyForPlatform(PlatformType.I3D_BAREMETAL),
                gameConfiguration.getData().getKafkaGameInstanceSupplyDefaultPartitionSize(),
                gameConfiguration.getData().getKafkaGameInstanceDefaultReplicationFactor());
    }
}
