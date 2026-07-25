package io.pedrini.expsplit.adapters.out.persistence.group;

import io.pedrini.expsplit.domain.group.model.MembershipStatus;
import io.pedrini.expsplit.domain.group.model.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import java.time.Instant;
import java.util.UUID;

@Embeddable
class MembershipEmbeddable {

    @Column(nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

    @Column(nullable = false)
    private Instant invitedAt;

    private Instant joinedAt;

    protected MembershipEmbeddable() { }

    MembershipEmbeddable(UUID userId, Role role, MembershipStatus status, Instant invitedAt, Instant joinedAt) {
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
    }

    UUID getUserId() { return userId; }
    Role getRole() { return role; }
    MembershipStatus getStatus() { return status; }
    Instant getInvitedAt() { return invitedAt; }
    Instant getJoinedAt() { return joinedAt; }
}
