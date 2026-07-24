package io.pedrini.expsplit.domain.user.model;

public record FirstName(String value) {

    private static final int MAX_LENGTH = 24;

    public FirstName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FirstName cannot be blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("FirstName cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
