package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionShareRequest(
        @NotNull UUID userId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) @DecimalMax("100.0") BigDecimal percentage
) {
}
