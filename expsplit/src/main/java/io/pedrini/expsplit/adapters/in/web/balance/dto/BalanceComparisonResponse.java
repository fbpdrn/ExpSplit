package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.domain.balance.model.BalanceComparison;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceComparisonResponse(UUID counterpartId, BigDecimal netAmount) {

    public static BalanceComparisonResponse from(BalanceComparison balance) {
        return new BalanceComparisonResponse(balance.counterpartId().id(), balance.netAmount());
    }
}
