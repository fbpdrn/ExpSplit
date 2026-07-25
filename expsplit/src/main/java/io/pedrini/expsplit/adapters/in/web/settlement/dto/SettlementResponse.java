package io.pedrini.expsplit.adapters.in.web.settlement.dto;

import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.transaction.model.Category;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SettlementResponse(UUID id, UUID groupId, UUID payerId, UUID payeeId, BigDecimal amount,
                                  Category category, Instant createdAt) {

    public static SettlementResponse from(Settlement settlement) {
        return new SettlementResponse(
                settlement.id().id(),
                settlement.groupId().id(),
                settlement.payerId().id(),
                settlement.payeeId().id(),
                settlement.amount().value(),
                settlement.category(),
                settlement.createdAt()
        );
    }
}
