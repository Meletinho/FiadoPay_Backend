package com.fiadopay.backend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.registry.AntiFraudRegistry;
import com.fiadopay.backend.registry.PaymentMethodRegistry;
import com.fiadopay.backend.registry.WebhookSinkRegistry;

@SpringBootTest
class AnnotationRegistryTests {

    @Autowired
    private PaymentMethodRegistry paymentMethodRegistry;

    @Autowired
    private AntiFraudRegistry antiFraudRegistry;

    @Autowired
    private WebhookSinkRegistry webhookSinkRegistry;

    @Test
    void paymentMethodRegistryHasCardHandler() {
        assertNotNull(paymentMethodRegistry.get(PaymentMethodType.CARD));
    }

    @Test
    void antiFraudRegistryHasHighAmountRule() {
        AntiFraudRegistry.Meta meta = antiFraudRegistry.get("HighAmount");
        assertNotNull(meta);
        assertTrue(meta.threshold >= 1000.0);
    }

    @Test
    void webhookSinkRegistryHasDefaultSink() {
        WebhookSinkRegistry.Meta meta = webhookSinkRegistry.get("Default");
        assertNotNull(meta);
        assertEquals("/webhook/provider", meta.endpoint);
    }
}