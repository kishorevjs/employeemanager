package com.leancam.employeemanager.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "bulkInsertExecutor")
    public Executor bulkInsertExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(4);      // threads always alive, ready to work
        executor.setMaxPoolSize(10);      // max threads created under heavy load
        executor.setQueueCapacity(200);   // tasks waiting when all threads are busy
        executor.setThreadNamePrefix("bulk-insert-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // CallerRunsPolicy: if queue is full, the calling thread does the work itself
        // This provides backpressure — slows the caller instead of dropping tasks

        executor.initialize();
        return executor;
    }
}