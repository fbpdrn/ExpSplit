package io.pedrini.expsplit.domain.user.exception;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class UserProfileAlreadyExistsException extends RuntimeException {

    public UserProfileAlreadyExistsException(UserProfileId id) {
        super("User profile already exists: " + id.id());
    }
}
