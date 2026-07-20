package io.pedrini.auth.domain.user.port.out;

import io.pedrini.auth.domain.user.model.UserAuthPassword;

public interface PasswordHasher {

    UserAuthPassword hash(String rawPassword);

    boolean matches(String rawPassword, UserAuthPassword hashedPassword);
}
