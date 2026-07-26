package io.pedrini.expsplit.adapters.in.web.group.dto;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.group.model.Membership;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.time.Instant;
import java.util.Map;

public record MembershipResponse(UserProfileResponse user, String role, String status, Instant invitedAt, Instant joinedAt) {

    public static MembershipResponse from(Membership membership, Map<UserProfileId, UserProfileResponse> users) {
        return new MembershipResponse(
                users.get(membership.userId()),
                membership.role().name(),
                membership.status().name(),
                membership.invitedAt(),
                membership.joinedAt()
        );
    }
}
