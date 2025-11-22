package com.fiadopay.backend.webhook;

import com.fiadopay.backend.registry.WebhookSinkRegistry;
import com.fiadopay.backend.spi.WebhookSinkHandler;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;

@Component
public class WebhookDispatcher {
    private final ExecutorService executorService;
    private final WebhookSinkRegistry webhookSinkRegistry;

    public WebhookDispatcher(ExecutorService executorService, WebhookSinkRegistry webhookSinkRegistry) {
        this.executorService = executorService;
        this.webhookSinkRegistry = webhookSinkRegistry;
    }

    public void dispatch(String name, String payload) {
        WebhookSinkRegistry.Meta meta = webhookSinkRegistry.get(name);
        if (meta == null) {
            return;
        }
        executorService.submit(() -> {
            try {
                WebhookSinkHandler handler = meta.type.getDeclaredConstructor().newInstance();
                handler.handle(payload);
            } catch (Exception ignored) {
            }
        });
    }
}