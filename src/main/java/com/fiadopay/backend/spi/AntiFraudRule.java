package com.fiadopay.backend.spi;

import com.fiadopay.backend.entity.Payment;

public interface AntiFraudRule {
    boolean validate(Payment payment);
}