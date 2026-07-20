package io.pedrini.auth.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAuthPasswordTest {

    @Test
    void blankException() {
        assertThatThrownBy(() -> new UserAuthPassword("  ", PasswordAlgorithm.ARGON2ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullException() {
        assertThatThrownBy(() -> new UserAuthPassword(null, PasswordAlgorithm.ARGON2ID))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullAlgorithmException() {
        assertThatThrownBy(() -> new UserAuthPassword("hash-value", null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
