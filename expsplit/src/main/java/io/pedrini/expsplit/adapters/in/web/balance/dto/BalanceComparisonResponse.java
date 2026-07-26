package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.balance.model.BalanceComparison;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceComparisonResponse(UserProfileResponse counterpart, BigDecimal netAmount) {

    public static BalanceComparisonResponse from(BalanceComparison balance, Map<UserProfileId, UserProfileResponse> users) {
        return new BalanceComparisonResponse(users.get(balance.counterpartId()), balance.netAmount());
    }
}
