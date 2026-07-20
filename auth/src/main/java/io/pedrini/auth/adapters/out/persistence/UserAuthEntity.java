package io.pedrini.auth.adapters.out.persistence;

import io.pedrini.auth.domain.user.model.PasswordAlgorithm;
import io.pedrini.auth.domain.user.model.UserAuthStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_auth")
class UserAuthEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PasswordAlgorithm passwordAlgorithm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserAuthStatus status;

    @Column(nullable = false)
    private int failedAttempts;

    private Instant lockedUntil;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserAuthEntity() {
    }

    UserAuthEntity(UUID id, String email, String passwordHash, PasswordAlgorithm passwordAlgorithm,
                   UserAuthStatus status, int failedAttempts, Instant lockedUntil, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.passwordAlgorithm = passwordAlgorithm;
        this.status = status;
        this.failedAttempts = failedAttempts;
        this.lockedUntil = lockedUntil;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getPasswordHash() { return passwordHash; }
    PasswordAlgorithm getPasswordAlgorithm() { return passwordAlgorithm; }
    UserAuthStatus getStatus() { return status; }
    int getFailedAttempts() { return failedAttempts; }
    Instant getLockedUntil() { return lockedUntil; }
    Instant getCreatedAt() { return createdAt; }
}
