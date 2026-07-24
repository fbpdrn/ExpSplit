package io.pedrini.expsplit.adapters.in.web.user.dto;

import io.pedrini.expsplit.domain.user.model.UserProfile;

import java.util.UUID;

public record UserProfileResponse(UUID id, String email, String firstName, String lastName) {

    public static UserProfileResponse from(UserProfile userProfile) {
        return new UserProfileResponse(
                userProfile.id().id(),
                userProfile.email().email(),
                userProfile.firstName() != null ? userProfile.firstName().value() : null,
                userProfile.lastName() != null ? userProfile.lastName().value() : null
        );
    }
}
