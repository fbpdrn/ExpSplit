package io.pedrini.expsplit.domain.group.model;

import io.pedrini.expsplit.domain.group.exception.AlreadyGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.NoPendingInvitationException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.NotGroupOwnerException;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Group {

    private final GroupId id;
    private final GroupName name;
    private final List<Membership> members;
    private final Instant createdAt;

    public Group(GroupId id, GroupName name, List<Membership> members, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>(members);
        this.createdAt = createdAt;
    }

    public static Group create(GroupId id, GroupName name, UserProfileId ownerId) {
        List<Membership> members = new ArrayList<>();
        members.add(Membership.owner(ownerId, Instant.now()));
        return new Group(id, name, members, Instant.now());
    }

    public void invite(UserProfileId inviterId, UserProfileId inviteeId) {
        requireOwner(inviterId);

        if (findMembership(inviteeId).isPresent()) {
            throw new AlreadyGroupMemberException(id, inviteeId);
        }

        members.add(Membership.invited(inviteeId, Instant.now()));
    }

    public void acceptInvitation(UserProfileId userId) {
        pendingMembership(userId).accept(Instant.now());
    }

    public void rejectInvitation(UserProfileId userId) {
        members.remove(pendingMembership(userId));
    }

    public boolean leave(UserProfileId userId) {
        Membership membership = acceptedMembership(userId);
        members.remove(membership);

        if (membership.isOwner()) {
            acceptedMembers().stream()
                    .min(Comparator.comparing(Membership::joinedAt))
                    .ifPresent(Membership::promoteToOwner);
        }

        return acceptedMembers().isEmpty();
    }

    public void requireOwner(UserProfileId userId) {
        if (!isOwner(userId)) {
            throw new NotGroupOwnerException(id, userId);
        }
    }

    public boolean isOwner(UserProfileId userId) {
        return findMembership(userId).map(Membership::isOwner).orElse(false);
    }

    public boolean isMember(UserProfileId userId) {
        return findMembership(userId).isPresent();
    }

    private List<Membership> acceptedMembers() {
        return members.stream().filter(Membership::isAccepted).toList();
    }

    private Optional<Membership> findMembership(UserProfileId userId) {
        return members.stream().filter(m -> m.userId().equals(userId)).findFirst();
    }

    private Membership pendingMembership(UserProfileId userId) {
        return findMembership(userId)
                .filter(Membership::isPending)
                .orElseThrow(() -> new NoPendingInvitationException(id, userId));
    }

    private Membership acceptedMembership(UserProfileId userId) {
        return findMembership(userId)
                .filter(Membership::isAccepted)
                .orElseThrow(() -> new NotGroupMemberException(id, userId));
    }

    public GroupId id() { return id; }
    public GroupName name() { return name; }
    public List<Membership> members() { return List.copyOf(members); }
    public Instant createdAt() { return createdAt; }
}
