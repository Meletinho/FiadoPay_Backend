package com.fiadopay.backend.registry;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.spi.PaymentMethodHandler;

@Component
public class PaymentMethodRegistry {
    private final Map<PaymentMethodType, Class<? extends PaymentMethodHandler>> registry = new EnumMap<>(PaymentMethodType.class);

    public void register(PaymentMethodType type, Class<? extends PaymentMethodHandler> handler) {
        registry.put(type, handler);
    }

    public Class<? extends PaymentMethodHandler> get(PaymentMethodType type) {
        return registry.get(type);
    }

    public Map<PaymentMethodType, Class<? extends PaymentMethodHandler>> all() {
        return Collections.unmodifiableMap(registry);
    }
}