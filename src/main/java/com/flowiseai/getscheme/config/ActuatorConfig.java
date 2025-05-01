package com.flowiseai.getscheme.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ActuatorConfig {
    private static final Logger logger = LoggerFactory.getLogger(ActuatorConfig.class);

    @Bean
    public HealthIndicator pingHealthIndicator() {
        logger.info("Registering ping health indicator");
        return () -> {
            logger.debug("Ping health check called");
            return Health.up().build();
        };
    }
} 