package io.pedrini.expsplit.domain.group.model;

public record GroupName(String value) {

    private static final int MAX_LENGTH = 100;

    public GroupName {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Name cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
