package io.pedrini.expsplit.domain.balance.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
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

class BalanceCalculatorTest {

    private static final GroupId GROUP_ID = GroupId.generate();
    private static final UserProfileId A = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId B = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId C = new UserProfileId(UUID.randomUUID());
    private static final TransactionDescription DESCRIPTION = new TransactionDescription("Expense");

    private static TransactionShare share(UserProfileId userId, String percentage) {
        return new TransactionShare(userId, new SharePercentage(new BigDecimal(percentage)));
    }

    private static Transaction expense(UserProfileId paidBy, String amount, TransactionShare... shares) {
        return new Transaction(TransactionId.generate(), GROUP_ID, paidBy, DESCRIPTION, new Amount(new BigDecimal(amount)),
                Category.OTHER, List.of(shares), Instant.now());
    }

    private static Settlement settlement(UserProfileId payer, UserProfileId payee, String amount) {
        return new Settlement(SettlementId.generate(), GROUP_ID, payer, payee, new Amount(new BigDecimal(amount)), Category.OTHER, Instant.now());
    }

    @Test
    void expenseSplitEvenlyBetweenPayerAndOneOther() {
        Transaction t = expense(A, "100", share(A, "50"), share(B, "50"));

        BalanceCalculator calculator = BalanceCalculator.of(List.of(t), List.of());

        assertThat(calculator.netBalance(A)).isEqualByComparingTo("50");
        assertThat(calculator.netBalance(B)).isEqualByComparingTo("-50");
        assertThat(calculator.netOwedTo(B, A)).isEqualByComparingTo("50");
        assertThat(calculator.netOwedTo(A, B)).isEqualByComparingTo("-50");
    }

    @Test
    void expenseWherePayerHasNoShare() {
        Transaction t = expense(A, "100", share(B, "50"), share(C, "50"));

        BalanceCalculator calculator = BalanceCalculator.of(List.of(t), List.of());

        assertThat(calculator.netBalance(A)).isEqualByComparingTo("100");
        assertThat(calculator.netBalance(B)).isEqualByComparingTo("-50");
        assertThat(calculator.netBalance(C)).isEqualByComparingTo("-50");
    }

    @Test
    void settlementFullyCancelsMatchingDebt() {
        Transaction t = expense(A, "100", share(A, "50"), share(B, "50"));
        Settlement s = settlement(B, A, "50");

        BalanceCalculator calculator = BalanceCalculator.of(List.of(t), List.of(s));

        assertThat(calculator.netBalance(A)).isEqualByComparingTo("0");
        assertThat(calculator.netBalance(B)).isEqualByComparingTo("0");
        assertThat(calculator.netOwedTo(B, A)).isEqualByComparingTo("0");
    }

    @Test
    void multipleTransactionsNetOutAcrossBothDirections() {
        Transaction aPaysForBoth = expense(A, "100", share(A, "50"), share(B, "50"));
        Transaction bPaysForBoth = expense(B, "40", share(A, "50"), share(B, "50"));

        BalanceCalculator calculator = BalanceCalculator.of(List.of(aPaysForBoth, bPaysForBoth), List.of());

        assertThat(calculator.netOwedTo(B, A)).isEqualByComparingTo("30");
        assertThat(calculator.netOwedTo(A, B)).isEqualByComparingTo("-30");
        assertThat(calculator.netBalance(A)).isEqualByComparingTo("30");
        assertThat(calculator.netBalance(B)).isEqualByComparingTo("-30");
    }

    @Test
    void netBalancesAcrossGroupAlwaysSumToZero() {
        Transaction t1 = expense(A, "100", share(A, "34"), share(B, "33"), share(C, "33"));
        Transaction t2 = expense(B, "60", share(A, "50"), share(C, "50"));
        Settlement s = settlement(C, A, "10");

        BalanceCalculator calculator = BalanceCalculator.of(List.of(t1, t2), List.of(s));

        BigDecimal total = calculator.netBalance(A).add(calculator.netBalance(B)).add(calculator.netBalance(C));
        assertThat(total).isEqualByComparingTo("0");
    }
}
