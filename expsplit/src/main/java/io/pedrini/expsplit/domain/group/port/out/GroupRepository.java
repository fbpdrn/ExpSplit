package io.pedrini.expsplit.domain.group.port.out;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;
import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(GroupId id);

    List<Group> findPendingInvitations(UserProfileId userId);

    List<Group> findAcceptedGroups(UserProfileId userId);

    void deleteById(GroupId id);
}
