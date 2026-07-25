package io.pedrini.expsplit.domain.settlement.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface GetSettlementUseCase {

    Settlement get(GroupId groupId, SettlementId settlementId, UserProfileId requesterId);
}
