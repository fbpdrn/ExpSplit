package io.pedrini.expsplit.domain.balance.port.in;

import io.pedrini.expsplit.domain.balance.model.Balance;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface GetGroupBalanceUseCase {

    List<Balance> getGroupBalance(GroupId groupId, UserProfileId requesterId);
}
