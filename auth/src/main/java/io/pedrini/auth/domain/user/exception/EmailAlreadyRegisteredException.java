package io.pedrini.auth.domain.user.exception;

import io.pedrini.auth.domain.user.model.UserAuthEmail;

public class EmailAlreadyRegisteredException extends RuntimeException {

    public EmailAlreadyRegisteredException(UserAuthEmail email) {
        super("Email already registered: " + email.email());
    }
}
