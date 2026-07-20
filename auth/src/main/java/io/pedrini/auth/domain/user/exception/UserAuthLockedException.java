package io.pedrini.auth.domain.user.exception;

import io.pedrini.auth.domain.user.model.UserAuthId;

public class UserAuthLockedException extends RuntimeException {

    public UserAuthLockedException(UserAuthId id) {
        super("User is locked");
    }
}
