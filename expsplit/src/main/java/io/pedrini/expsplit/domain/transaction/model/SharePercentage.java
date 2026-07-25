package io.pedrini.expsplit.domain.transaction.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record SharePercentage(BigDecimal value) {

    public SharePercentage {
        if (value == null) {
            throw new IllegalArgumentException("Percentage cannot be null");
        }

        value = value.setScale(2, RoundingMode.HALF_UP);

        if (value.compareTo(BigDecimal.ZERO) <= 0 || value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage must be greater than 0 and at most 100");
        }
    }
}
