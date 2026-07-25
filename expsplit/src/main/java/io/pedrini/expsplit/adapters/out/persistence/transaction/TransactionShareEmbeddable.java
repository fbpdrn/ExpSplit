package io.pedrini.expsplit.adapters.out.persistence.transaction;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
class TransactionShareEmbeddable {

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal percentage;

    protected TransactionShareEmbeddable() { }

    TransactionShareEmbeddable(UUID userId, BigDecimal percentage) {
        this.userId = userId;
        this.percentage = percentage;
    }

    UUID getUserId() { return userId; }
    BigDecimal getPercentage() { return percentage; }
}
