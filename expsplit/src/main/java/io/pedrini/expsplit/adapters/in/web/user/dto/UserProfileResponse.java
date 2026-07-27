package io.pedrini.expsplit.adapters.in.web.user.dto;

import io.pedrini.expsplit.adapters.in.web.group.dto.GroupSummaryResponse;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.user.model.UserProfile;

import java.util.List;
import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String firstName, String lastName, List<GroupSummaryResponse> groups) {

    public static UserProfileResponse from(UserProfile userProfile) {
        return from(userProfile, List.of());
    }

    public static UserProfileResponse from(UserProfile userProfile, List<Group> groups) {
        return new UserProfileResponse(
                userProfile.id().id(),
                userProfile.email().email(),
                userProfile.firstName() != null ? userProfile.firstName().value() : null,
                userProfile.lastName() != null ? userProfile.lastName().value() : null,
                groups.stream().map(GroupSummaryResponse::from).toList()
        );
    }
}
