package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record TransactionResponse(UUID id, UUID groupId, UserProfileResponse paidBy, String description, BigDecimal amount,
                                   Category category, List<TransactionShareResponse> shares, Instant createdAt) {

    public static TransactionResponse from(Transaction transaction, Map<UserProfileId, UserProfileResponse> users) {
        return new TransactionResponse(
                transaction.id().id(),
                transaction.groupId().id(),
                users.get(transaction.paidBy()),
                transaction.description().value(),
                transaction.amount().value(),
                transaction.category(),
                transaction.shares().stream().map(share -> TransactionShareResponse.from(share, users)).toList(),
                transaction.createdAt()
        );
    }
}
