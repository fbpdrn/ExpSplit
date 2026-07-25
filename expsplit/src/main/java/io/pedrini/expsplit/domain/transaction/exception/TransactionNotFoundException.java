package io.pedrini.expsplit.domain.transaction.exception;

import io.pedrini.expsplit.domain.transaction.model.TransactionId;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(TransactionId id) {
        super("Transaction not found: " + id.id());
    }
}
