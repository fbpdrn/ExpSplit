package io.pedrini.expsplit.domain.group.exception;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class AlreadyGroupMemberException extends RuntimeException {

    public AlreadyGroupMemberException(GroupId groupId, UserProfileId userId) {
        super("User " + userId.id() + " is already a member of group " + groupId.id());
    }
}
