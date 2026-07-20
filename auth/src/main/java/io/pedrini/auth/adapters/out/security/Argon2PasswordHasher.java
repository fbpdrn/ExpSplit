package io.pedrini.auth.adapters.out.security;

import io.pedrini.auth.domain.user.model.PasswordAlgorithm;
import io.pedrini.auth.domain.user.model.UserAuthPassword;
import io.pedrini.auth.domain.user.port.out.PasswordHasher;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
class Argon2PasswordHasher implements PasswordHasher {

    private final Argon2PasswordEncoder argon2Encoder = Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();

    @Override
    public UserAuthPassword hash(String rawPassword) {
        return new UserAuthPassword(argon2Encoder.encode(rawPassword), PasswordAlgorithm.ARGON2ID);
    }

    @Override
    public boolean matches(String rawPassword, UserAuthPassword hashedPassword) {
        return argon2Encoder.matches(rawPassword, hashedPassword.hash());
    }
}
