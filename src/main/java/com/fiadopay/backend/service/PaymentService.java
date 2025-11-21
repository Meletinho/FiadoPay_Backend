package com.fiadopay.backend.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiadopay.backend.dto.PaymentRequest;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentStatus;
import com.fiadopay.backend.registry.AntiFraudRegistry;
import com.fiadopay.backend.repository.PaymentRepository;
import com.fiadopay.backend.spi.AntiFraudRule;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AntiFraudRegistry antiFraudRegistry;

    public PaymentService(PaymentRepository paymentRepository, AntiFraudRegistry antiFraudRegistry) {
        this.paymentRepository = paymentRepository;
        this.antiFraudRegistry = antiFraudRegistry;
    }

    @Transactional
    public Payment create(PaymentRequest request, String idempotencyKey) {
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            return existing.get();
        }
        Payment p = new Payment();
        p.setAmount(request.getAmount());
        p.setCurrency(request.getCurrency());
        p.setMethod(request.getMethod());
        p.setInstallments(request.getInstallments());
        p.setInterestRate(BigDecimal.ZERO);
        p.setTotalAmount(request.getAmount());
        p.setStatus(PaymentStatus.PENDING);
        p.setIdempotencyKey(idempotencyKey);
        processPayment(p);
        return paymentRepository.save(p);
    }

    public void processPayment(Payment payment) {
        List<String> reasons = new ArrayList<>();
        for (AntiFraudRegistry.Meta meta : antiFraudRegistry.all().values()) {
            try {
                AntiFraudRule rule = meta.type.getDeclaredConstructor().newInstance();
                AntiFraudRule.Result res = rule.validate(payment, meta.threshold);
                if (!res.isApproved()) {
                    reasons.add(res.getReason());
                }
            } catch (Exception e) {
            }
        }
        if (!reasons.isEmpty()) {
            payment.setStatus(PaymentStatus.DECLINED);
            payment.setDeclineReason(String.join("; ", reasons));
        }
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
}