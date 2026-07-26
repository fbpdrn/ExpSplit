package io.pedrini.expsplit.adapters.in.web.balance.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.balance.model.UserBalanceDetail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record UserBalanceResponse(UserProfileResponse user, BigDecimal netAmount, List<BalanceComparisonResponse> perCounterpart) {

    public static UserBalanceResponse from(UserBalanceDetail detail, Map<UserProfileId, UserProfileResponse> users) {
        return new UserBalanceResponse(
                users.get(detail.userId()),
                detail.netAmount(),
                detail.perCounterpart().stream().map(comparison -> BalanceComparisonResponse.from(comparison, users)).toList()
        );
    }
}
