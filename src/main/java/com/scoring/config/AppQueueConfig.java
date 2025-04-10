package com.scoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.topic")
public class AppQueueConfig {
    private String listenTopic;
    private String publishTopic;
}
