package com.fiadopay.backend.service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fiadopay.backend.dto.PaymentRequest;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentStatus;
import com.fiadopay.backend.registry.AntiFraudRegistry;
import com.fiadopay.backend.repository.PaymentRepository;
import com.fiadopay.backend.spi.AntiFraudRule;

@Service
public class PaymentService {
    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final AntiFraudRegistry antiFraudRegistry;
    private final java.util.concurrent.ExecutorService executorService;

    public PaymentService(PaymentRepository paymentRepository, AntiFraudRegistry antiFraudRegistry, java.util.concurrent.ExecutorService executorService) {
        this.paymentRepository = paymentRepository;
        this.antiFraudRegistry = antiFraudRegistry;
        this.executorService = executorService;
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
        BigDecimal interestRate = new BigDecimal("0.01");
        p.setInterestRate(interestRate);
        p.setTotalAmount(calculateTotal(request.getAmount(), request.getInstallments()));
        p.setStatus(PaymentStatus.PENDING);
        p.setIdempotencyKey(idempotencyKey);
        Payment saved;
        try {
            saved = paymentRepository.save(p);
        } catch (org.springframework.dao.DataIntegrityViolationException ex) {
            // Race condition on idempotency: return the existing record
            return paymentRepository.findByIdempotencyKey(idempotencyKey).orElseThrow();
        }
        UUID id = saved.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executorService.submit(() -> {
                    try {
                        paymentRepository.findById(id).ifPresent(PaymentService.this::processPayment);
                    } catch (Exception e) {
                        log.error("processPayment error", e);
                    }
                });
            }
        });
        return saved;
    }

    private BigDecimal calculateTotal(BigDecimal amount, int installments) {
        BigDecimal rate = new BigDecimal("0.01");
        BigDecimal onePlusRate = BigDecimal.ONE.add(rate);
        MathContext mc = new MathContext(20);
        BigDecimal factor = onePlusRate.pow(installments, mc);
        BigDecimal total = amount.multiply(factor, mc);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    public void processPayment(Payment payment) {
        List<String> reasons = new ArrayList<>();
        log.info("antifraud rules={} amount={}", antiFraudRegistry.all().size(), payment.getAmount());
        if (antiFraudRegistry.all().isEmpty()) {
            if (payment.getAmount() != null && payment.getAmount().compareTo(new BigDecimal("1000")) > 0) {
                reasons.add("HighAmount: " + payment.getAmount() + " > 1000.0");
            }
        } else {
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
        }
        if (!reasons.isEmpty()) {
            payment.setStatus(PaymentStatus.DECLINED);
            payment.setDeclineReason(String.join("; ", reasons));
            paymentRepository.save(payment);
        }
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
}