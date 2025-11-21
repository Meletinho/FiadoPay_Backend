package com.fiadopay.backend;

import com.fiadopay.backend.webhook.WebhookSignatureService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WebhookSignatureTests {
    @Autowired
    WebhookSignatureService svc;

    @Test
    void invalidSignatureFails() {
        String body = "{\"paymentId\":\"00000000-0000-0000-0000-000000000000\",\"status\":\"APPROVED\"}";
        boolean ok = svc.isValid(body, "deadbeef");
        org.junit.jupiter.api.Assertions.assertFalse(ok);
    }
}