package io.pedrini.expsplit.domain.group.port.out;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;

import java.util.Optional;

public interface GroupRepository {

    Group save(Group group);

    Optional<Group> findById(GroupId id);

    void deleteById(GroupId id);
}
