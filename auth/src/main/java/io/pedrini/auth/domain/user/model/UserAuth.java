package io.pedrini.auth.domain.user.model;

import java.time.Duration;
import java.time.Instant;

public class UserAuth {

    protected static final int MAX_FAILED_ATTEMPTS = 5;
    protected static final Duration LOCK_DURATION = Duration.ofMinutes(15);

    private final UserAuthId id;
    private final UserAuthEmail email;
    private UserAuthPassword password;
    private UserAuthStatus status;
    private int failedAttempts;
    private Instant lockedUntil;
    private final Instant createdAt;

    public UserAuth(UserAuthId id, UserAuthEmail email, UserAuthPassword password, UserAuthStatus status, int failedAttempts, Instant lockedUntil, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
    }

    public static UserAuth register(UserAuthEmail email, UserAuthPassword password) {
        return new UserAuth(UserAuthId.generate(), email, password, UserAuthStatus.ACTIVE, 0, null, Instant.now());
    }

    public void loginFailed() {
        failedAttempts++;
        if (failedAttempts >= MAX_FAILED_ATTEMPTS) {
            status = UserAuthStatus.LOCKED;
            lockedUntil = Instant.now().plus(LOCK_DURATION);
        }
    }

    public void loginSuccessful() {
        failedAttempts = 0;
        lockedUntil = null;
        if(status == UserAuthStatus.LOCKED) {
            status = UserAuthStatus.ACTIVE;
        }
    }

    public void disable() {
        status = UserAuthStatus.DISABLED;
    }

    public boolean isLocked() {
        return switch(status) {
            case DISABLED -> true;
            case LOCKED -> lockedUntil != null && Instant.now().isBefore(lockedUntil);
            case ACTIVE -> false;
        };
    }

    public void changePassword(UserAuthPassword password) {
        this.password = password;
    }

    public UserAuthId id() { return id; }
    public UserAuthEmail email() { return email; }
    public UserAuthPassword password() { return password; }
    public UserAuthStatus status() { return status; }
    public int failedAttempts() { return failedAttempts; }
    public Instant lockedUntil() { return lockedUntil; }
    public Instant createdAt() { return createdAt; }
}
