package io.pedrini.expsplit.domain.transaction.exception;

import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class TransactionPermissionException extends RuntimeException {

    public TransactionPermissionException(TransactionId transactionId, UserProfileId userId) {
        super("User " + userId.id() + " is not allowed to modify transaction " + transactionId.id());
    }
}
