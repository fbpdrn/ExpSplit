package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.Transaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(UUID id, UUID groupId, UUID paidBy, String description, BigDecimal amount,
                                   Category category, List<TransactionShareResponse> shares, Instant createdAt) {

    public static TransactionResponse from(Transaction transaction) {
        return new TransactionResponse(
                transaction.id().id(),
                transaction.groupId().id(),
                transaction.paidBy().id(),
                transaction.description().value(),
                transaction.amount().value(),
                transaction.category(),
                transaction.shares().stream().map(TransactionShareResponse::from).toList(),
                transaction.createdAt()
        );
    }
}
