package io.pedrini.expsplit.domain.transaction.model;

import java.util.UUID;

public record TransactionId(UUID id) {

    public TransactionId {
        if (id == null) {
            throw new IllegalArgumentException("TransactionId cannot be null");
        }
    }

    public static TransactionId generate() {
        return new TransactionId(UUID.randomUUID());
    }
}
