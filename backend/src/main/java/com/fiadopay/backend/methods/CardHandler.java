package com.fiadopay.backend.methods;

import com.fiadopay.backend.annotations.PaymentMethod;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentMethodType;
import com.fiadopay.backend.spi.PaymentMethodHandler;

@PaymentMethod(type = PaymentMethodType.CARD)
public class CardHandler implements PaymentMethodHandler {
    @Override
    public Payment handle(Payment payment) {
        return payment;
    }
}