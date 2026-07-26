package io.pedrini.expsplit.application.group;

import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.model.GroupName;
import io.pedrini.expsplit.domain.group.port.in.AcceptInvitationUseCase;
import io.pedrini.expsplit.domain.group.port.in.CreateGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.DeleteGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.GetGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.InviteMemberUseCase;
import io.pedrini.expsplit.domain.group.port.in.LeaveGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.ListPendingInvitationsUseCase;
import io.pedrini.expsplit.domain.group.port.in.RejectInvitationUseCase;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.user.exception.UserProfileNotFoundException;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;

import java.util.List;

public class GroupService implements CreateGroupUseCase, GetGroupUseCase, InviteMemberUseCase,
        AcceptInvitationUseCase, RejectInvitationUseCase, LeaveGroupUseCase, DeleteGroupUseCase, ListPendingInvitationsUseCase {

    private final GroupRepository groupRepository;
    private final UserProfileRepository userProfileRepository;

    public GroupService(GroupRepository groupRepository, UserProfileRepository userProfileRepository) {
        this.groupRepository = groupRepository;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public Group create(GroupName name, UserProfileId ownerId) {
        Group group = Group.create(GroupId.generate(), name, ownerId);
        return groupRepository.save(group);
    }

    @Override
    public Group get(GroupId groupId, UserProfileId requesterId) {
        Group group = load(groupId);
        if (!group.isMember(requesterId)) {
            throw new NotGroupMemberException(groupId, requesterId);
        }
        return group;
    }

    @Override
    public Group invite(GroupId groupId, UserProfileId inviterId, UserProfileId inviteeId) {
        Group group = load(groupId);

        if (userProfileRepository.findById(inviteeId).isEmpty()) {
            throw new UserProfileNotFoundException(inviteeId);
        }

        group.invite(inviterId, inviteeId);
        return groupRepository.save(group);
    }

    @Override
    public Group accept(GroupId groupId, UserProfileId userId) {
        Group group = load(groupId);
        group.acceptInvitation(userId);
        return groupRepository.save(group);
    }

    @Override
    public Group reject(GroupId groupId, UserProfileId userId) {
        Group group = load(groupId);
        group.rejectInvitation(userId);
        return groupRepository.save(group);
    }

    @Override
    public void leave(GroupId groupId, UserProfileId userId) {
        Group group = load(groupId);
        boolean shouldDelete = group.leave(userId);

        if (shouldDelete) {
            groupRepository.deleteById(groupId);
        } else {
            groupRepository.save(group);
        }
    }

    @Override
    public void delete(GroupId groupId, UserProfileId requesterId) {
        Group group = load(groupId);
        group.requireOwner(requesterId);
        groupRepository.deleteById(groupId);
    }

    @Override
    public List<Group> list(UserProfileId userId) {
        return groupRepository.findPendingInvitations(userId);
    }

    private Group load(GroupId groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }
}
