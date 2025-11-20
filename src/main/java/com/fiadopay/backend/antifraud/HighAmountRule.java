package com.fiadopay.backend.antifraud;

import com.fiadopay.backend.annotations.AntiFraud;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.spi.AntiFraudRule;

@AntiFraud(name = "HighAmount", threshold = 1000.0)
public class HighAmountRule implements AntiFraudRule {
    @Override
    public boolean validate(Payment payment) {
        return true;
    }
}