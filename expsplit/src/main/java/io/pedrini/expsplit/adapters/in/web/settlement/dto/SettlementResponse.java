package io.pedrini.expsplit.adapters.in.web.settlement.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SettlementResponse(UUID id, UUID groupId, UserProfileResponse payer, UserProfileResponse payee, BigDecimal amount,
                                  Category category, Instant createdAt) {

    public static SettlementResponse from(Settlement settlement, Map<UserProfileId, UserProfileResponse> users) {
        return new SettlementResponse(
                settlement.id().id(),
                settlement.groupId().id(),
                users.get(settlement.payerId()),
                users.get(settlement.payeeId()),
                settlement.amount().value(),
                settlement.category(),
                settlement.createdAt()
        );
    }
}
