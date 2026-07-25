package io.pedrini.expsplit.domain.transaction.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.exception.TransactionPermissionException;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TransactionTest {

    private static final GroupId GROUP_ID = GroupId.generate();
    private static final UserProfileId PAYER = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId OTHER_MEMBER = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId STRANGER = new UserProfileId(UUID.randomUUID());
    private static final TransactionDescription DESCRIPTION = new TransactionDescription("Cena");
    private static final Amount AMOUNT = new Amount(new BigDecimal("100"));

    private static TransactionShare share(UserProfileId userId, String percentage) {
        return new TransactionShare(userId, new SharePercentage(new BigDecimal(percentage)));
    }

    @Test
    void createSucceedsWhenSharesSumToHundred() {
        List<TransactionShare> shares = List.of(share(PAYER, "50"), share(OTHER_MEMBER, "50"));

        Transaction transaction = Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares);

        assertThat(transaction.shares()).hasSize(2);
        assertThat(transaction.paidBy()).isEqualTo(PAYER);
    }

    @Test
    void createEmptySharesException() {
        assertThatThrownBy(() -> Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createDuplicateShareException() {
        List<TransactionShare> shares = List.of(share(PAYER, "50"), share(PAYER, "50"));

        assertThatThrownBy(() -> Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createSharesNotSummingToHundredException() {
        List<TransactionShare> shares = List.of(share(PAYER, "50"), share(OTHER_MEMBER, "30"));

        assertThatThrownBy(() -> Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void createAbsorbsRoundingDeficitIntoLastShare() {
        List<TransactionShare> shares = List.of(share(PAYER, "33.33"), share(OTHER_MEMBER, "33.33"), share(STRANGER, "33.33"));

        Transaction transaction = Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares);

        BigDecimal total = transaction.shares().stream()
                .map(s -> s.percentage().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("100");
        assertThat(transaction.shares().get(2).percentage().value()).isEqualByComparingTo("33.34");
    }

    @Test
    void createAbsorbsRoundingSurplusIntoLastShare() {
        List<TransactionShare> shares = List.of(share(PAYER, "33.34"), share(OTHER_MEMBER, "33.34"), share(STRANGER, "33.34"));

        Transaction transaction = Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares);

        BigDecimal total = transaction.shares().stream()
                .map(s -> s.percentage().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(total).isEqualByComparingTo("100");
        assertThat(transaction.shares().get(2).percentage().value()).isEqualByComparingTo("33.32");
    }

    @Test
    void createBeyondToleranceStillThrows() {
        List<TransactionShare> shares = List.of(share(PAYER, "33.33"), share(OTHER_MEMBER, "33.33"), share(STRANGER, "33.00"));

        assertThatThrownBy(() -> Transaction.create(TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, shares))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateReplacesFields() {
        Transaction transaction = Transaction.create(
                TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, List.of(share(PAYER, "100")));

        TransactionDescription newDescription = new TransactionDescription("Pranzo");
        Amount newAmount = new Amount(new BigDecimal("60"));
        List<TransactionShare> newShares = List.of(share(PAYER, "40"), share(OTHER_MEMBER, "60"));

        transaction.update(newDescription, newAmount, newShares);

        assertThat(transaction.description()).isEqualTo(newDescription);
        assertThat(transaction.amount()).isEqualTo(newAmount);
        assertThat(transaction.shares()).containsExactlyInAnyOrderElementsOf(newShares);
    }

    @Test
    void requireModifiableAllowsPayer() {
        Transaction transaction = Transaction.create(
                TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, List.of(share(PAYER, "100")));

        transaction.requireModifiable(PAYER, false);
    }

    @Test
    void requireModifiableAllowsGroupOwner() {
        Transaction transaction = Transaction.create(
                TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, List.of(share(PAYER, "100")));

        transaction.requireModifiable(OTHER_MEMBER, true);
    }

    @Test
    void requireModifiableRejectsStranger() {
        Transaction transaction = Transaction.create(
                TransactionId.generate(), GROUP_ID, PAYER, DESCRIPTION, AMOUNT, List.of(share(PAYER, "100")));

        assertThatThrownBy(() -> transaction.requireModifiable(STRANGER, false))
                .isInstanceOf(TransactionPermissionException.class);
    }
}
