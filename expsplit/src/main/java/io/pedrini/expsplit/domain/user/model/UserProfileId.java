package io.pedrini.expsplit.domain.user.model;

import java.util.UUID;

public record UserProfileId(UUID id) {

    public UserProfileId {
        if (id == null) {
            throw new IllegalArgumentException("UserProfileId cannot be null");
        }
    }
}
