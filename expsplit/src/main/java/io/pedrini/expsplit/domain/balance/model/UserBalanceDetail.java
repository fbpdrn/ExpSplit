package io.pedrini.expsplit.domain.balance.model;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.util.List;

public record UserBalanceDetail(UserProfileId userId, BigDecimal netAmount, List<BalanceComparison> perCounterpart) {
}
