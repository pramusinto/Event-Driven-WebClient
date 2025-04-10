package com.scoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.auth")
public class AppAuthConfig {
    private String username;
    private String password;
    private String bodyUsername;
    private String bodyPassword;
}
