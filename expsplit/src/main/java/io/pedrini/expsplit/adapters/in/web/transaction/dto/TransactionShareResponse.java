package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import io.pedrini.expsplit.domain.transaction.model.TransactionShare;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionShareResponse(UUID userId, BigDecimal percentage) {

    public static TransactionShareResponse from(TransactionShare share) {
        return new TransactionShareResponse(share.userId().id(), share.percentage().value());
    }
}
