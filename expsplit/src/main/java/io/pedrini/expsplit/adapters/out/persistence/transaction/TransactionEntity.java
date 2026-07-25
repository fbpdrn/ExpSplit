package io.pedrini.expsplit.adapters.out.persistence.transaction;

import io.pedrini.expsplit.domain.transaction.model.Category;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "transactions")
class TransactionEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private UUID groupId;

    @Column(nullable = false)
    private UUID paidBy;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "transaction_share", joinColumns = @JoinColumn(name = "transaction_id"))
    private List<TransactionShareEmbeddable> shares = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected TransactionEntity() { }

    TransactionEntity(UUID id, UUID groupId, UUID paidBy, String description, BigDecimal amount, Category category,
                       List<TransactionShareEmbeddable> shares, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.shares = shares;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    UUID getGroupId() { return groupId; }
    UUID getPaidBy() { return paidBy; }
    String getDescription() { return description; }
    BigDecimal getAmount() { return amount; }
    Category getCategory() { return category; }
    List<TransactionShareEmbeddable> getShares() { return shares; }
    Instant getCreatedAt() { return createdAt; }
}
