package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.domain.group.model.Group;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GroupResponse(UUID id, String name, List<MembershipResponse> members, Instant createdAt) {

    public static GroupResponse from(Group group) {
        return new GroupResponse(
                group.id().id(),
                group.name().value(),
                group.members().stream().map(MembershipResponse::from).toList(),
                group.createdAt()
        );
    }
}
