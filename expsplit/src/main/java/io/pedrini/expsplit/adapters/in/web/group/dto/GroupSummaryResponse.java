package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.domain.group.model.Group;

import java.util.UUID;

public record GroupSummaryResponse(UUID id, String name) {

    public static GroupSummaryResponse from(Group group) {
        return new GroupSummaryResponse(group.id().id(), group.name().value());
    }
}
