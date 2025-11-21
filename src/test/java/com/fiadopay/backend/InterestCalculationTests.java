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
class InterestCalculationTests {
    @Autowired
    PaymentService paymentService;

    @Test
    void totalAmountWithInterest() {
        PaymentRequest r = new PaymentRequest();
        r.setAmount(new BigDecimal("100.00"));
        r.setCurrency("BRL");
        r.setMethod(PaymentMethodType.CARD);
        r.setInstallments(12);
        Payment p = paymentService.create(r, java.util.UUID.randomUUID().toString());
        assertEquals(new BigDecimal("0.01"), p.getInterestRate());
        assertTrue(p.getTotalAmount().compareTo(new BigDecimal("100.00")) > 0);
        assertEquals(2, p.getTotalAmount().scale());
    }
}