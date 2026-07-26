package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record GroupResponse(UUID id, String name, List<MembershipResponse> members, Instant createdAt) {

    public static GroupResponse from(Group group, Map<UserProfileId, UserProfileResponse> users) {
        return new GroupResponse(
                group.id().id(),
                group.name().value(),
                group.members().stream().map(member -> MembershipResponse.from(member, users)).toList(),
                group.createdAt()
        );
    }
}
