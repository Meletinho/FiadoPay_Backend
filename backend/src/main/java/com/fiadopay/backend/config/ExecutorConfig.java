package com.fiadopay.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ExecutorConfig {
    @Bean
    public ExecutorService executorService() {
        int n = Math.max(2, Runtime.getRuntime().availableProcessors());
        Logger log = LoggerFactory.getLogger(ExecutorConfig.class);
        ThreadFactory tf = r -> {
            Thread t = new Thread(r);
            t.setName("fiadopay-exec-" + t.getId());
            t.setDaemon(true);
            return t;
        };
        return new ThreadPoolExecutor(n, n, 60L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), tf) {
            @Override
            protected void beforeExecute(Thread t, Runnable r) {
                log.debug("task-start {}", t.getName());
            }
            @Override
            protected void afterExecute(Runnable r, Throwable t) {
                if (t != null) {
                    log.error("task-error", t);
                } else {
                    log.debug("task-end");
                }
            }
        };
    }
}