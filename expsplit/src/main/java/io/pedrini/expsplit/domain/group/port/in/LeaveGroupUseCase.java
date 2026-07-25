package io.pedrini.expsplit.domain.group.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface LeaveGroupUseCase {

    void leave(GroupId groupId, UserProfileId userId);
}
