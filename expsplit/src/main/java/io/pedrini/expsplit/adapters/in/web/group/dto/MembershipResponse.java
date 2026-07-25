package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.domain.group.model.Membership;

import java.time.Instant;
import java.util.UUID;

public record MembershipResponse(UUID userId, String role, String status, Instant invitedAt, Instant joinedAt) {

    public static MembershipResponse from(Membership membership) {
        return new MembershipResponse(
                membership.userId().id(),
                membership.role().name(),
                membership.status().name(),
                membership.invitedAt(),
                membership.joinedAt()
        );
    }
}
