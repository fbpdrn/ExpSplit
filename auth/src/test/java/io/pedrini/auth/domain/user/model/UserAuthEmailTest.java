package io.pedrini.auth.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAuthEmailTest {

    @Test
    void normalizeLowerCase() {
        UserAuthEmail email = new UserAuthEmail("Test@Example.COM");

        assertThat(email.email()).isEqualTo("test@example.com");
    }

    @Test
    void normalizeWhiteSpaces() {
        UserAuthEmail email = new UserAuthEmail("  test@example.com  ");

        assertThat(email.email()).isEqualTo("test@example.com");
    }

    @Test
    void invalidFormatException() {
        assertThatThrownBy(() -> new UserAuthEmail("not-an-email"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UserAuthEmail("not @ email"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new UserAuthEmail("not@email."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullException() {
        assertThatThrownBy(() -> new UserAuthEmail(null))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
