package io.pedrini.auth.domain.user.model;

import java.util.UUID;

public record UserAuthId(UUID id) {

    public UserAuthId {
        if(id == null) {
            throw new IllegalArgumentException("UserAuthId cannot be null");
        }
    }

    public static UserAuthId generate() {
        return new UserAuthId(UUID.randomUUID());
    }
}
