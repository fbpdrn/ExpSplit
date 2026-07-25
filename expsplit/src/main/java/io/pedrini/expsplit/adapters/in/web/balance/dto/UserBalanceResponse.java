package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.domain.balance.model.UserBalanceDetail;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record UserBalanceResponse(UUID userId, BigDecimal netAmount, List<BalanceComparisonResponse> perCounterpart) {

    public static UserBalanceResponse from(UserBalanceDetail detail) {
        return new UserBalanceResponse(
                detail.userId().id(),
                detail.netAmount(),
                detail.perCounterpart().stream().map(BalanceComparisonResponse::from).toList()
        );
    }
}
