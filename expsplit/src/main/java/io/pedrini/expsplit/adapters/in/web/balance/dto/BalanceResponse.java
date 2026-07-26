package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.balance.model.Balance;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.util.Map;

public record BalanceResponse(UserProfileResponse user, BigDecimal netAmount) {

    public static BalanceResponse from(Balance balance, Map<UserProfileId, UserProfileResponse> users) {
        return new BalanceResponse(users.get(balance.userId()), balance.netAmount());
    }
}
