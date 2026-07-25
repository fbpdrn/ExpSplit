package io.pedrini.expsplit.domain.transaction.model;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

public record TransactionShare(UserProfileId userId, SharePercentage percentage) {

    public TransactionShare {
        if (userId == null) {
            throw new IllegalArgumentException("userId cannot be null");
        }

        if (percentage == null) {
            throw new IllegalArgumentException("percentage cannot be null");
        }
    }
}
