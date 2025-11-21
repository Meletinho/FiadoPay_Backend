package com.fiadopay.backend.spi;

import com.fiadopay.backend.entity.Payment;

public interface PaymentMethodHandler {
    Payment handle(Payment payment);
}