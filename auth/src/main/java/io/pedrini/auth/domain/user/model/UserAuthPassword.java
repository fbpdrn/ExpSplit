package io.pedrini.auth.domain.user.model;

public record UserAuthPassword(String hash, PasswordAlgorithm algorithm) {

    public UserAuthPassword {
        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException("Hash cannot be null or blank");
        }

        if (algorithm == null) {
            throw new IllegalArgumentException("Algorithm cannot be null");
        }
    }
}
