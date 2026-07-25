package io.pedrini.expsplit.adapters.in.web.statistics.dto;

import io.pedrini.expsplit.domain.statistics.model.CategoryStatistic;
import io.pedrini.expsplit.domain.transaction.model.Category;

import java.math.BigDecimal;

public record CategoryStatisticResponse(Category category, BigDecimal totalAmount, long transactionCount) {

    public static CategoryStatisticResponse from(CategoryStatistic statistic) {
        return new CategoryStatisticResponse(statistic.category(), statistic.totalAmount(), statistic.transactionCount());
    }
}
