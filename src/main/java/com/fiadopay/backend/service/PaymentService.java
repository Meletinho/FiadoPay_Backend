package com.fiadopay.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fiadopay.backend.dto.PaymentRequest;
import com.fiadopay.backend.entity.Payment;
import com.fiadopay.backend.entity.PaymentStatus;
import com.fiadopay.backend.repository.PaymentRepository;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment create(PaymentRequest request, String idempotencyKey) {
        Payment p = new Payment();
        p.setAmount(request.getAmount());
        p.setCurrency(request.getCurrency());
        p.setMethod(request.getMethod());
        p.setInstallments(request.getInstallments());
        p.setInterestRate(BigDecimal.ZERO);
        p.setTotalAmount(request.getAmount());
        p.setStatus(PaymentStatus.PENDING);
        p.setIdempotencyKey(idempotencyKey);
        return paymentRepository.save(p);
    }

    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id);
    }

    public List<Payment> findAll() {
        return paymentRepository.findAll();
    }
}