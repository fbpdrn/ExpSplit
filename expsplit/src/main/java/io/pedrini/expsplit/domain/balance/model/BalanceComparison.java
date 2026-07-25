package io.pedrini.expsplit.domain.balance.model;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;

public record BalanceComparison(UserProfileId counterpartId, BigDecimal netAmount) {
}
