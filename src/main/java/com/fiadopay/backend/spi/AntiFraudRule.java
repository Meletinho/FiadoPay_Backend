package com.fiadopay.backend.spi;

import com.fiadopay.backend.entity.Payment;

public interface AntiFraudRule {
    public static class Result {
        private final boolean approved;
        private final String reason;

        public Result(boolean approved, String reason) {
            this.approved = approved;
            this.reason = reason;
        }

        public boolean isApproved() {
            return approved;
        }

        public String getReason() {
            return reason;
        }
    }

    Result validate(Payment payment, double threshold);
}