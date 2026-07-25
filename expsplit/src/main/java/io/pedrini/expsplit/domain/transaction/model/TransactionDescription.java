package io.pedrini.expsplit.domain.transaction.model;

public record TransactionDescription(String value) {

    private static final int MAX_LENGTH = 200;

    public TransactionDescription {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }

        value = value.trim();

        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Description cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
