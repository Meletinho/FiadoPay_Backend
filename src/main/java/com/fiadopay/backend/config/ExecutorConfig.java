package com.fiadopay.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class ExecutorConfig {
    @Bean
    public ExecutorService executorService() {
        return Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
    }
}