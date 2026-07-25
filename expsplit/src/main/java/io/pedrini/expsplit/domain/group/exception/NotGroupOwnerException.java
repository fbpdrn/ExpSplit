package io.pedrini.expsplit.domain.group.exception;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class NotGroupOwnerException extends RuntimeException {

    public NotGroupOwnerException(GroupId groupId, UserProfileId userId) {
        super("User " + userId.id() + " is not the owner of group " + groupId.id());
    }
}
