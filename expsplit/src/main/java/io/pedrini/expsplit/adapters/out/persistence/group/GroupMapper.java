package io.pedrini.expsplit.adapters.out.persistence.group;

import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.model.GroupName;
import io.pedrini.expsplit.domain.group.model.Membership;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

class GroupMapper {

    static Group toDomain(GroupEntity entity) {
        List<Membership> members = entity.getMembers().stream()
                .map(m -> new Membership(
                        new UserProfileId(m.getUserId()),
                        m.getRole(),
                        m.getStatus(),
                        m.getInvitedAt(),
                        m.getJoinedAt()
                ))
                .toList();

        return new Group(
                new GroupId(entity.getId()),
                new GroupName(entity.getName()),
                members,
                entity.getCreatedAt()
        );
    }

    static GroupEntity toEntity(Group group) {
        List<MembershipEmbeddable> members = group.members().stream()
                .map(m -> new MembershipEmbeddable(
                        m.userId().id(),
                        m.role(),
                        m.status(),
                        m.invitedAt(),
                        m.joinedAt()
                ))
                .toList();

        return new GroupEntity(
                group.id().id(),
                group.name().value(),
                members,
                group.createdAt()
        );
    }
}
