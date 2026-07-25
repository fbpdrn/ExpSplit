package io.pedrini.expsplit.domain.settlement.exception;

import io.pedrini.expsplit.domain.settlement.model.SettlementId;

public class SettlementNotFoundException extends RuntimeException {

    public SettlementNotFoundException(SettlementId id) {
        super("Settlement not found: " + id.id());
    }
}
