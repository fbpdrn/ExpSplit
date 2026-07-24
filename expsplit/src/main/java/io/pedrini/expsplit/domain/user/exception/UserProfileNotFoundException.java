package io.pedrini.expsplit.domain.user.exception;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class UserProfileNotFoundException extends RuntimeException {

    public UserProfileNotFoundException(UserProfileId id) {
        super("User profile not found: " + id.id());
    }
}
