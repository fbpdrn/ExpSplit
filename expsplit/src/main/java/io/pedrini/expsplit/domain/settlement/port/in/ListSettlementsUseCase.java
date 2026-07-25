package io.pedrini.expsplit.domain.settlement.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface ListSettlementsUseCase {

    List<Settlement> list(GroupId groupId, UserProfileId requesterId);
}
