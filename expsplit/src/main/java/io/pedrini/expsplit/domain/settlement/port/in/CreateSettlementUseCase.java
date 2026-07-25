package io.pedrini.expsplit.domain.settlement.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface CreateSettlementUseCase {

    Settlement create(GroupId groupId, UserProfileId payerId, UserProfileId payeeId, Amount amount, Category category);
}
