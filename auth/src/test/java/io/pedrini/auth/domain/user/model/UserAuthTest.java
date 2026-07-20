package io.pedrini.auth.domain.user.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthTest {

    private static final UserAuthEmail EMAIL = new UserAuthEmail("test@example.com");
    private static final UserAuthPassword PASSWORD = new UserAuthPassword("hashed-value", PasswordAlgorithm.ARGON2ID);

    @Test
    void createUser() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);

        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.ACTIVE);
        assertThat(userAuth.failedAttempts()).isZero();
        assertThat(userAuth.lockedUntil()).isNull();
        assertThat(userAuth.isLocked()).isFalse();
    }

    @Test
    void loginFailedMaxAttempts() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);

        // Si verifica per ogni tentativo di login fallito fino al massimo consentito
        for (int i = 0; i <= UserAuth.MAX_FAILED_ATTEMPTS; i++) {
            userAuth.loginFailed();
            assertThat(userAuth.failedAttempts()).isEqualTo(i+1);

            if(i >= UserAuth.MAX_FAILED_ATTEMPTS - 1) {
                assertThat(userAuth.status()).isEqualTo(UserAuthStatus.LOCKED);
                assertThat(userAuth.isLocked()).isTrue();
            } else {
                assertThat(userAuth.status()).isEqualTo(UserAuthStatus.ACTIVE);
                assertThat(userAuth.isLocked()).isFalse();
            }
        }
    }

    @Test
    void loginSuccessfulLockExpired() {
        // Si crea un utente con uno stato di blocco scaduto di un minuto
        UserAuth userAuth = new UserAuth(
                UserAuthId.generate(), EMAIL, PASSWORD, UserAuthStatus.LOCKED,
                5, Instant.now().minus(1, ChronoUnit.MINUTES), Instant.now()
        );

        assertThat(userAuth.isLocked()).isFalse();
        // Lo stato di blocco si aggiorna al primo login di successo
        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.LOCKED);
    }

    @Test
    void loginSuccessfulResetLock() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);
        for (int i = 0; i < UserAuth.MAX_FAILED_ATTEMPTS; i++) {
            userAuth.loginFailed();
        }
        assertThat(userAuth.isLocked()).isTrue();

        userAuth.loginSuccessful();

        assertThat(userAuth.failedAttempts()).isZero();
        assertThat(userAuth.lockedUntil()).isNull();
        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.ACTIVE);
        assertThat(userAuth.isLocked()).isFalse();
    }

    @Test
    void disable() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);

        userAuth.disable();

        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.DISABLED);
        assertThat(userAuth.isLocked()).isTrue();
    }

    @Test
    void disableLogin() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);
        userAuth.disable();

        userAuth.loginSuccessful();

        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.DISABLED);
        assertThat(userAuth.isLocked()).isTrue();
    }

    @Test
    void changePassword() {
        UserAuth userAuth = UserAuth.register(EMAIL, PASSWORD);
        UserAuthPassword newPassword = new UserAuthPassword("newPasswordHash", PasswordAlgorithm.ARGON2ID);

        userAuth.changePassword(newPassword);

        assertThat(userAuth.password()).isEqualTo(newPassword);
    }
}
