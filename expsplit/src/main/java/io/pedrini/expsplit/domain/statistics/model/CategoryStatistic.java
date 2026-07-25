package io.pedrini.expsplit.domain.statistics.model;

import io.pedrini.expsplit.domain.transaction.model.Category;

import java.math.BigDecimal;

public record CategoryStatistic(Category category, BigDecimal totalAmount, long transactionCount) {
}
