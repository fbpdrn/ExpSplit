package io.pedrini.expsplit.domain.group.exception;

import io.pedrini.expsplit.domain.group.model.GroupId;

public class GroupNotFoundException extends RuntimeException {

    public GroupNotFoundException(GroupId id) {
        super("Group not found: " + id.id());
    }
}
