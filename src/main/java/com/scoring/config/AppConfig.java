package com.scoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private AppQueueConfig topic;
    private AppAuthConfig auth;
    private String scoringUrl;
    private String scoringDefaultPath;
    private Long timeout;
    private Long intervalScheduler;
    private Integer webclientRetryCount;
    private Long webclientRetryDelay;
}
