package com.scoring.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@ConfigurationProperties(prefix = "threadpool")
@Data
public class KafkaThreadPoolConfig {
    private Integer corePoolSize;
    private Integer maxPoolSize;
    private Integer queueCapacity;

    @Bean(name = "kafkaTaskExecutorProduct")
    public ThreadPoolTaskExecutor productTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity+100);
        executor.setThreadNamePrefix("scoring-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "kafkaTaskExecutorScoring")
    public ThreadPoolTaskExecutor scoringTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("scoring-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "kafkaTaskExecutorGetReport")
    public ThreadPoolTaskExecutor getReportTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix("gReport-");
        executor.initialize();
        return executor;
    }
}