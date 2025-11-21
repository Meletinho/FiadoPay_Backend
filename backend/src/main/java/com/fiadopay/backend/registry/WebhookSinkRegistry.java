package com.fiadopay.backend.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fiadopay.backend.spi.WebhookSinkHandler;

@Component
public class WebhookSinkRegistry {
    public static class Meta {
        public final String name;
        public final String endpoint;
        public final Class<? extends WebhookSinkHandler> type;

        public Meta(String name, String endpoint, Class<? extends WebhookSinkHandler> type) {
            this.name = name;
            this.endpoint = endpoint;
            this.type = type;
        }
    }

    private final Map<String, Meta> registry = new HashMap<>();

    public void register(String name, String endpoint, Class<? extends WebhookSinkHandler> type) {
        registry.put(name, new Meta(name, endpoint, type));
    }

    public Meta get(String name) {
        return registry.get(name);
    }

    public Map<String, Meta> all() {
        return Collections.unmodifiableMap(registry);
    }
}