package io.pedrini.expsplit.domain.group.model;

import io.pedrini.expsplit.domain.group.exception.AlreadyGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.NoPendingInvitationException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.exception.NotGroupOwnerException;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupTest {

    private static final UserProfileId OWNER = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId MEMBER = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId STRANGER = new UserProfileId(UUID.randomUUID());
    private static final GroupName NAME = new GroupName("Vacanze");

    @Test
    void createHasSingleAcceptedOwner() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        assertThat(group.members()).hasSize(1);
        assertThat(group.isOwner(OWNER)).isTrue();
        assertThat(group.members().getFirst().status()).isEqualTo(MembershipStatus.ACCEPTED);
    }

    @Test
    void inviteAddsPendingMember() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        group.invite(OWNER, MEMBER);

        assertThat(group.isMember(MEMBER)).isTrue();
        assertThat(group.members()).filteredOn(m -> m.userId().equals(MEMBER))
                .singleElement()
                .satisfies(m -> {
                    assertThat(m.status()).isEqualTo(MembershipStatus.PENDING);
                    assertThat(m.role()).isEqualTo(Role.MEMBER);
                });
    }

    @Test
    void inviteByNonOwnerThrows() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);
        group.invite(OWNER, MEMBER);
        group.acceptInvitation(MEMBER);

        assertThatThrownBy(() -> group.invite(MEMBER, STRANGER))
                .isInstanceOf(NotGroupOwnerException.class);
    }

    @Test
    void inviteAlreadyMemberThrows() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        assertThatThrownBy(() -> group.invite(OWNER, OWNER))
                .isInstanceOf(AlreadyGroupMemberException.class);
    }

    @Test
    void acceptInvitation() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);
        group.invite(OWNER, MEMBER);

        group.acceptInvitation(MEMBER);

        assertThat(group.members()).filteredOn(m -> m.userId().equals(MEMBER))
                .singleElement()
                .satisfies(m -> assertThat(m.status()).isEqualTo(MembershipStatus.ACCEPTED));
    }

    @Test
    void acceptWithoutInvitationThrows() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        assertThatThrownBy(() -> group.acceptInvitation(MEMBER))
                .isInstanceOf(NoPendingInvitationException.class);
    }

    @Test
    void rejectInvitationRemovesMembership() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);
        group.invite(OWNER, MEMBER);

        group.rejectInvitation(MEMBER);

        assertThat(group.isMember(MEMBER)).isFalse();
    }

    @Test
    void rejectWithoutInvitationThrows() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        assertThatThrownBy(() -> group.rejectInvitation(MEMBER))
                .isInstanceOf(NoPendingInvitationException.class);
    }

    @Test
    void leaveAsMember() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);
        group.invite(OWNER, MEMBER);
        group.acceptInvitation(MEMBER);

        boolean shouldDelete = group.leave(MEMBER);

        assertThat(shouldDelete).isFalse();
        assertThat(group.isMember(MEMBER)).isFalse();
        assertThat(group.isOwner(OWNER)).isTrue();
    }

    @Test
    void leaveNotMemberThrows() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        assertThatThrownBy(() -> group.leave(STRANGER))
                .isInstanceOf(NotGroupMemberException.class);
    }

    @Test
    void leaveAsOwnerAloneSignalsDeletion() {
        Group group = Group.create(GroupId.generate(), NAME, OWNER);

        boolean shouldDelete = group.leave(OWNER);

        assertThat(shouldDelete).isTrue();
        assertThat(group.members()).isEmpty();
    }

    @Test
    void leaveAsOwnerPromotesEarliestJoinedMember() {
        Instant t0 = Instant.parse("2026-01-01T00:00:00Z");
        UserProfileId later = new UserProfileId(UUID.randomUUID());

        List<Membership> members = new ArrayList<>();
        members.add(new Membership(OWNER, Role.OWNER, MembershipStatus.ACCEPTED, t0, t0));
        members.add(new Membership(later, Role.MEMBER, MembershipStatus.ACCEPTED, t0, t0.plusSeconds(20)));
        members.add(new Membership(MEMBER, Role.MEMBER, MembershipStatus.ACCEPTED, t0, t0.plusSeconds(10)));
        Group group = new Group(GroupId.generate(), NAME, members, t0);

        boolean shouldDelete = group.leave(OWNER);

        assertThat(shouldDelete).isFalse();
        assertThat(group.isOwner(MEMBER)).isTrue();
        assertThat(group.isOwner(later)).isFalse();
    }

    @Test
    void leaveAsOwnerIgnoresPendingInviteesForDeletionCheck() {
        Instant t0 = Instant.now();

        List<Membership> members = new ArrayList<>();
        members.add(new Membership(OWNER, Role.OWNER, MembershipStatus.ACCEPTED, t0, t0));
        members.add(new Membership(MEMBER, Role.MEMBER, MembershipStatus.PENDING, t0, null));
        Group group = new Group(GroupId.generate(), NAME, members, t0);

        boolean shouldDelete = group.leave(OWNER);

        assertThat(shouldDelete).isTrue();
    }
}
