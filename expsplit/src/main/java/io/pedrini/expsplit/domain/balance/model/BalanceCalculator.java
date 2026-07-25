package io.pedrini.expsplit.domain.balance.model;

import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BalanceCalculator {

    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private record DebtKey(UserProfileId debtor, UserProfileId creditor) {
    }

    private final Map<UserProfileId, BigDecimal> netBalance = new HashMap<>();
    private final Map<DebtKey, BigDecimal> pairwiseDebt = new HashMap<>();

    private BalanceCalculator() {
    }

    public static BalanceCalculator of(List<Transaction> transactions, List<Settlement> settlements) {
        BalanceCalculator calculator = new BalanceCalculator();

        for (Transaction transaction : transactions) {
            for (TransactionShare share : transaction.shares()) {
                if (share.userId().equals(transaction.paidBy())) {
                    continue;
                }
                BigDecimal owed = transaction.amount().value()
                        .multiply(share.percentage().value())
                        .divide(HUNDRED, 2, RoundingMode.HALF_UP);
                calculator.addDebt(share.userId(), transaction.paidBy(), owed);
            }
        }

        for (Settlement settlement : settlements) {
            calculator.addDebt(settlement.payerId(), settlement.payeeId(), settlement.amount().value().negate());
        }

        return calculator;
    }

    private void addDebt(UserProfileId debtor, UserProfileId creditor, BigDecimal amount) {
        pairwiseDebt.merge(new DebtKey(debtor, creditor), amount, BigDecimal::add);
        netBalance.merge(creditor, amount, BigDecimal::add);
        netBalance.merge(debtor, amount.negate(), BigDecimal::add);
    }

    public BigDecimal netBalance(UserProfileId userId) {
        return netBalance.getOrDefault(userId, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal netOwedTo(UserProfileId from, UserProfileId to) {
        BigDecimal forward = pairwiseDebt.getOrDefault(new DebtKey(from, to), BigDecimal.ZERO);
        BigDecimal backward = pairwiseDebt.getOrDefault(new DebtKey(to, from), BigDecimal.ZERO);
        return forward.subtract(backward).setScale(2, RoundingMode.HALF_UP);
    }
}
