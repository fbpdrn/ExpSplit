package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.Membership;

import java.time.Instant;
import java.util.UUID;

public record PendingInvitationResponse(UUID groupId, String groupName, Instant invitedAt) {

    public static PendingInvitationResponse from(Group group, Membership membership) {
        return new PendingInvitationResponse(group.id().id(), group.name().value(), membership.invitedAt());
    }
}
