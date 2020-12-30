package com.han.startup.common;

import lombok.Getter;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ServerHealth implements HealthIndicator {
    @Getter
    AtomicInteger healthIndicator = new AtomicInteger(0);

    @Override
    public Health health() {
        if (healthIndicator.get() != 0) {
            return Health.down()
                    .withDetail("Error Code", healthIndicator.get()).build();
        }
        return Health.up().build();
    }
}
