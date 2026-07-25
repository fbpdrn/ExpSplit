package io.pedrini.expsplit.domain.statistics.model;

import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.Transaction;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsCalculator {

    private StatisticsCalculator() {
    }

    public static List<CategoryStatistic> summarize(List<Transaction> transactions) {
        Map<Category, List<Transaction>> byCategory = transactions.stream()
                .collect(Collectors.groupingBy(Transaction::category));

        return byCategory.entrySet().stream()
                .map(entry -> new CategoryStatistic(
                        entry.getKey(),
                        entry.getValue().stream().map(t -> t.amount().value()).reduce(BigDecimal.ZERO, BigDecimal::add),
                        entry.getValue().size()
                ))
                .sorted(Comparator.comparing(CategoryStatistic::totalAmount).reversed())
                .toList();
    }
}
