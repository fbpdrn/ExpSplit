package io.pedrini.expsplit.domain.settlement.model;

import java.util.UUID;

public record SettlementId(UUID id) {

    public SettlementId {
        if (id == null) {
            throw new IllegalArgumentException("SettlementId cannot be null");
        }
    }

    public static SettlementId generate() {
        return new SettlementId(UUID.randomUUID());
    }
}
