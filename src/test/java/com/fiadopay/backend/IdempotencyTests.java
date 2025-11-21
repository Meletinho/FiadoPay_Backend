package com.fiadopay.backend;

import com.fiadopay.backend.dto.PaymentRequest;
import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.service.PaymentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class IdempotencyTests {
    @Autowired
    PaymentService paymentService;

    @Test
    void sameIdempotencyReturnsSamePayment() {
        String key = java.util.UUID.randomUUID().toString();
        PaymentRequest r = new PaymentRequest();
        r.setAmount(new BigDecimal("50.00"));
        r.setCurrency("BRL");
        r.setMethod(PaymentMethodType.CARD);
        r.setInstallments(1);
        Payment p1 = paymentService.create(r, key);
        Payment p2 = paymentService.create(r, key);
        assertEquals(p1.getId(), p2.getId());
    }
}