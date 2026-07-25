package io.pedrini.expsplit.domain.settlement.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.exception.SettlementPermissionException;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SettlementTest {

    private static final GroupId GROUP_ID = GroupId.generate();
    private static final UserProfileId PAYER = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId PAYEE = new UserProfileId(UUID.randomUUID());
    private static final UserProfileId STRANGER = new UserProfileId(UUID.randomUUID());
    private static final Amount AMOUNT = new Amount(new BigDecimal("25"));

    @Test
    void createSucceeds() {
        Settlement settlement = Settlement.create(SettlementId.generate(), GROUP_ID, PAYER, PAYEE, AMOUNT);

        assertThat(settlement.payerId()).isEqualTo(PAYER);
        assertThat(settlement.payeeId()).isEqualTo(PAYEE);
        assertThat(settlement.amount()).isEqualTo(AMOUNT);
    }

    @Test
    void createSamePayerAndPayeeException() {
        assertThatThrownBy(() -> Settlement.create(SettlementId.generate(), GROUP_ID, PAYER, PAYER, AMOUNT))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requireDeletableAllowsPayer() {
        Settlement settlement = Settlement.create(SettlementId.generate(), GROUP_ID, PAYER, PAYEE, AMOUNT);

        settlement.requireDeletable(PAYER, false);
    }

    @Test
    void requireDeletableAllowsGroupOwner() {
        Settlement settlement = Settlement.create(SettlementId.generate(), GROUP_ID, PAYER, PAYEE, AMOUNT);

        settlement.requireDeletable(STRANGER, true);
    }

    @Test
    void requireDeletableRejectsStranger() {
        Settlement settlement = Settlement.create(SettlementId.generate(), GROUP_ID, PAYER, PAYEE, AMOUNT);

        assertThatThrownBy(() -> settlement.requireDeletable(STRANGER, false))
                .isInstanceOf(SettlementPermissionException.class);
    }
}
