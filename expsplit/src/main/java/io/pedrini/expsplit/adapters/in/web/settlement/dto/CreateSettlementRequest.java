package io.pedrini.expsplit.adapters.in.web.settlement.dto;

import io.pedrini.expsplit.domain.transaction.model.Category;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateSettlementRequest(
        @NotNull UUID payeeId,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        Category category
) {
}
