package io.pedrini.expsplit.domain.statistics.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.SharePercentage;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionDescription;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsCalculatorTest {

    private static final GroupId GROUP_ID = GroupId.generate();
    private static final UserProfileId PAYER = new UserProfileId(UUID.randomUUID());
    private static final TransactionDescription DESCRIPTION = new TransactionDescription("Expense");

    private static Transaction expense(String amount, Category category) {
        TransactionShare share = new TransactionShare(PAYER, new SharePercentage(new BigDecimal("100")));
        return new Transaction(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, new Amount(new BigDecimal(amount)),
                category, List.of(share), Instant.now());
    }

    @Test
    void emptyTransactionsProduceNoStatistics() {
        assertThat(StatisticsCalculator.summarize(List.of())).isEmpty();
    }

    @Test
    void sumsAmountsAndCountsPerCategory() {
        List<Transaction> transactions = List.of(
                expense("30", Category.FOOD),
                expense("20", Category.FOOD),
                expense("15", Category.TRANSPORT));

        List<CategoryStatistic> statistics = StatisticsCalculator.summarize(transactions);

        assertThat(statistics).hasSize(2);
        assertThat(statistics).filteredOn(s -> s.category() == Category.FOOD)
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.totalAmount()).isEqualByComparingTo("50");
                    assertThat(s.transactionCount()).isEqualTo(2);
                });
        assertThat(statistics).filteredOn(s -> s.category() == Category.TRANSPORT)
                .singleElement()
                .satisfies(s -> {
                    assertThat(s.totalAmount()).isEqualByComparingTo("15");
                    assertThat(s.transactionCount()).isEqualTo(1);
                });
    }

    @Test
    void onlyCategoriesWithTransactionsAreReturned() {
        List<CategoryStatistic> statistics = StatisticsCalculator.summarize(List.of(expense("10", Category.UTILITIES)));

        assertThat(statistics).singleElement()
                .satisfies(s -> assertThat(s.category()).isEqualTo(Category.UTILITIES));
    }

    @Test
    void sortedByTotalAmountDescending() {
        List<Transaction> transactions = List.of(
                expense("10", Category.FOOD),
                expense("100", Category.ACCOMMODATION),
                expense("50", Category.TRANSPORT));

        List<CategoryStatistic> statistics = StatisticsCalculator.summarize(transactions);

        assertThat(statistics).extracting(CategoryStatistic::category)
                .containsExactly(Category.ACCOMMODATION, Category.TRANSPORT, Category.FOOD);
    }

    @Test
    void usesFullExpenseAmountRegardlessOfShares() {
        TransactionShare half = new TransactionShare(PAYER, new SharePercentage(new BigDecimal("50")));
        TransactionShare otherHalf = new TransactionShare(new UserProfileId(UUID.randomUUID()), new SharePercentage(new BigDecimal("50")));
        Transaction split = new Transaction(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, new Amount(new BigDecimal("80")),
                Category.FOOD, List.of(half, otherHalf), Instant.now());

        List<CategoryStatistic> statistics = StatisticsCalculator.summarize(List.of(split));

        assertThat(statistics).singleElement()
                .satisfies(s -> assertThat(s.totalAmount()).isEqualByComparingTo("80"));
    }
}
