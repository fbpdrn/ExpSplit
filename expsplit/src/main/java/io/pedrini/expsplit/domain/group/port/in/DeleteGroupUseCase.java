package io.pedrini.expsplit.domain.group.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface DeleteGroupUseCase {

    void delete(GroupId groupId, UserProfileId requesterId);
}
