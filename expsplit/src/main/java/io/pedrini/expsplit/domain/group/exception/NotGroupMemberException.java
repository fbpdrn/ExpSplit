package io.pedrini.expsplit.domain.group.exception;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class NotGroupMemberException extends RuntimeException {

    public NotGroupMemberException(GroupId groupId, UserProfileId userId) {
        super("User " + userId.id() + " is not a member of group " + groupId.id());
    }
}
