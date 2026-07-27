package io.pedrini.expsplit.adapters.out.persistence.group;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class GroupRepository implements io.pedrini.expsplit.domain.group.port.out.GroupRepository {

    private final GroupJpaRepository jpaRepository;

    GroupRepository(GroupJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Group save(Group group) {
        GroupEntity saved = jpaRepository.save(GroupMapper.toEntity(group));
        return GroupMapper.toDomain(saved);
    }

    @Override
    public Optional<Group> findById(GroupId id) {
        return jpaRepository.findById(id.id()).map(GroupMapper::toDomain);
    }

    @Override
    public List<Group> findPendingInvitations(UserProfileId userId) {
        return jpaRepository.findPendingInvitations(userId.id()).stream().map(GroupMapper::toDomain).toList();
    }

    @Override
    public List<Group> findAcceptedGroups(UserProfileId userId) {
        return jpaRepository.findAcceptedGroups(userId.id()).stream().map(GroupMapper::toDomain).toList();
    }

    @Override
    public void deleteById(GroupId id) {
        jpaRepository.deleteById(id.id());
    }
}
