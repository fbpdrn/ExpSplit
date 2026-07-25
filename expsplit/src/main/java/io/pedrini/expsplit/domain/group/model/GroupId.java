package io.pedrini.expsplit.domain.group.model;

import java.util.UUID;

public record GroupId(UUID id) {

    public GroupId {
        if (id == null) {
            throw new IllegalArgumentException("GroupId cannot be null");
        }
    }

    public static GroupId generate() {
        return new GroupId(UUID.randomUUID());
    }
}
