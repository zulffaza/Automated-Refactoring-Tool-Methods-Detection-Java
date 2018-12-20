package com.finalproject.automated.refactoring.tool.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 9 December 2018
 */

@Configuration
@EnableAsync
@Profile("async")
public class AsyncConfig {

    private static final String THREAD_NAME_PREFIX = "Java Methods Detection -";

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        threadPoolTaskExecutor.setCorePoolSize(Integer.MAX_VALUE);
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
