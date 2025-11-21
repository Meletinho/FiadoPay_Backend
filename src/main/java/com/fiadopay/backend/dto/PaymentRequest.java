package com.fiadopay.backend.dto;

import java.math.BigDecimal;

import com.fiadopay.backend.entity.PaymentMethodType;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class PaymentRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    @Size(min = 3, max = 3)
    private String currency;

    @NotNull
    private PaymentMethodType method;

    @NotNull
    @Min(1)
    @Max(12)
    private Integer installments;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public PaymentMethodType getMethod() {
        return method;
    }

    public void setMethod(PaymentMethodType method) {
        this.method = method;
    }

    public Integer getInstallments() {
        return installments;
    }

    public void setInstallments(Integer installments) {
        this.installments = installments;
    }
}