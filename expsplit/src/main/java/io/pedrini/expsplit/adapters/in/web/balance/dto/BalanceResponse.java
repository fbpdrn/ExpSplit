package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.domain.balance.model.Balance;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID userId, BigDecimal netAmount) {

    public static BalanceResponse from(Balance balance) {
        return new BalanceResponse(balance.userId().id(), balance.netAmount());
    }
}
