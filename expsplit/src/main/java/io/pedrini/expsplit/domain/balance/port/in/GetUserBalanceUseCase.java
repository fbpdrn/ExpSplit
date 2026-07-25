package io.pedrini.expsplit.domain.balance.port.in;

import io.pedrini.expsplit.domain.balance.model.UserBalanceDetail;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface GetUserBalanceUseCase {

    UserBalanceDetail getUserBalance(GroupId groupId, UserProfileId requesterId);
}
