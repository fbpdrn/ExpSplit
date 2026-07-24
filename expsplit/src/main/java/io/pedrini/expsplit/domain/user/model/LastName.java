package io.pedrini.expsplit.domain.user.model;

public record LastName(String value) {

    private static final int MAX_LENGTH = 24;

    public LastName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("LastName cannot be blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("LastName cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
