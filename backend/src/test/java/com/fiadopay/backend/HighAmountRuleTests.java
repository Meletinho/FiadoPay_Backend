package com.fiadopay.backend;

import com.fiadopay.backend.antifraud.HighAmountRule;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.spi.AntiFraudRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class HighAmountRuleTests {
    @Test
    void declinesAboveThreshold() {
        Payment p = new Payment();
        p.setAmount(new BigDecimal("5000.00"));
        AntiFraudRule.Result res = new HighAmountRule().validate(p, 1000.0);
        assertFalse(res.isApproved());
    }
}