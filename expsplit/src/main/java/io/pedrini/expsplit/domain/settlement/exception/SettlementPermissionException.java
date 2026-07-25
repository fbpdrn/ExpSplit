package io.pedrini.expsplit.domain.settlement.exception;

import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class SettlementPermissionException extends RuntimeException {

    public SettlementPermissionException(SettlementId settlementId, UserProfileId userId) {
        super("User " + userId.id() + " is not allowed to modify settlement " + settlementId.id());
    }
}
