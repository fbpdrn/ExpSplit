package io.pedrini.expsplit.domain.transaction.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Amount(BigDecimal value) {

    public Amount {
        if (value == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }

        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        value = value.setScale(2, RoundingMode.HALF_UP);
    }
}
