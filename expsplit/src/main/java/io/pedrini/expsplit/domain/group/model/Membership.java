package io.pedrini.expsplit.domain.group.model;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.time.Instant;

public class Membership {

    private final UserProfileId userId;
    private Role role;
    private MembershipStatus status;
    private final Instant invitedAt;
    private Instant joinedAt;

    public Membership(UserProfileId userId, Role role, MembershipStatus status, Instant invitedAt, Instant joinedAt) {
        this.userId = userId;
        this.role = role;
        this.status = status;
        this.invitedAt = invitedAt;
        this.joinedAt = joinedAt;
    }

    static Membership owner(UserProfileId userId, Instant now) {
        return new Membership(userId, Role.OWNER, MembershipStatus.ACCEPTED, now, now);
    }

    static Membership invited(UserProfileId userId, Instant now) {
        return new Membership(userId, Role.MEMBER, MembershipStatus.PENDING, now, null);
    }

    void accept(Instant now) {
        this.status = MembershipStatus.ACCEPTED;
        this.joinedAt = now;
    }

    void promoteToOwner() {
        this.role = Role.OWNER;
    }

    public UserProfileId userId() { return userId; }
    public Role role() { return role; }
    public MembershipStatus status() { return status; }
    public Instant invitedAt() { return invitedAt; }
    public Instant joinedAt() { return joinedAt; }

    public boolean isOwner() { return role == Role.OWNER; }
    public boolean isAccepted() { return status == MembershipStatus.ACCEPTED; }
    public boolean isPending() { return status == MembershipStatus.PENDING; }
}
