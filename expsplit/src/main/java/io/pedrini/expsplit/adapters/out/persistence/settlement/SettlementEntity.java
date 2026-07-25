package io.pedrini.expsplit.adapters.out.persistence.settlement;

import io.pedrini.expsplit.domain.transaction.model.Category;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "settlements")
class SettlementEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID payerId;

    @Column(nullable = false)
    private UUID payeeId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected SettlementEntity() { }

    SettlementEntity(UUID id, UUID groupId, UUID payerId, UUID payeeId, BigDecimal amount, Category category, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    UUID getGroupId() { return groupId; }
    UUID getPayerId() { return payerId; }
    UUID getPayeeId() { return payeeId; }
    BigDecimal getAmount() { return amount; }
    Category getCategory() { return category; }
    Instant getCreatedAt() { return createdAt; }
}
