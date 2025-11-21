package com.fiadopay.backend.registry;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fiadopay.backend.spi.AntiFraudRule;

@Component
public class AntiFraudRegistry {
    public static class Meta {
        public final String name;
        public final double threshold;
        public final Class<? extends AntiFraudRule> type;

        public Meta(String name, double threshold, Class<? extends AntiFraudRule> type) {
            this.name = name;
            this.threshold = threshold;
            this.type = type;
        }
    }

    private final Map<String, Meta> registry = new HashMap<>();

    public void register(String name, double threshold, Class<? extends AntiFraudRule> type) {
        registry.put(name, new Meta(name, threshold, type));
    }

    public Meta get(String name) {
        return registry.get(name);
    }

    public Map<String, Meta> all() {
        return Collections.unmodifiableMap(registry);
    }
}