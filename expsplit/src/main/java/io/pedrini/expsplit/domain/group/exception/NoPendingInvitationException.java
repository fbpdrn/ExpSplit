package io.pedrini.expsplit.domain.group.exception;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public class NoPendingInvitationException extends RuntimeException {

    public NoPendingInvitationException(GroupId groupId, UserProfileId userId) {
        super("No pending invitation for user " + userId.id() + " in group " + groupId.id());
    }
}
