package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.util.Map;

public record TransactionShareResponse(UserProfileResponse user, BigDecimal percentage) {

    public static TransactionShareResponse from(TransactionShare share, Map<UserProfileId, UserProfileResponse> users) {
        return new TransactionShareResponse(users.get(share.userId()), share.percentage().value());
    }
}
