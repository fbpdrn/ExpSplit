package io.pedrini.expsplit.domain.group.port.in;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupName;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface CreateGroupUseCase {

    Group create(GroupName name, UserProfileId ownerId);
}
