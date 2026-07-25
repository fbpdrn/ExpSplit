package io.pedrini.expsplit.adapters.in.web.transaction.dto;

import io.pedrini.expsplit.domain.transaction.model.Category;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record UpdateTransactionRequest(
        @NotBlank String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = false) BigDecimal amount,
        Category category,
        @NotEmpty List<@Valid TransactionShareRequest> shares
) {
}
