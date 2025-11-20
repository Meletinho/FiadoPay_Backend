package com.fiadopay.backend.antifraud;

import java.math.BigDecimal;

import com.fiadopay.backend.annotations.AntiFraud;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.spi.AntiFraudRule;

@AntiFraud(name = "HighAmount", threshold = 1000.0)
public class HighAmountRule implements AntiFraudRule {
    @Override
    public Result validate(Payment payment, double threshold) {
        BigDecimal amount = payment.getAmount();
        if (amount != null && amount.doubleValue() > threshold) {
            String reason = "HighAmount: " + amount + " > " + threshold;
            return new Result(false, reason);
        }
        return new Result(true, null);
    }
}