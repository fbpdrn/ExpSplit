package io.pedrini.expsplit.adapters.out.persistence.settlement;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

class SettlementMapper {

    static Settlement toDomain(SettlementEntity entity) {
        return new Settlement(
                new SettlementId(entity.getId()),
                new GroupId(entity.getGroupId()),
                new UserProfileId(entity.getPayerId()),
                new UserProfileId(entity.getPayeeId()),
                new Amount(entity.getAmount()),
                entity.getCreatedAt()
        );
    }

    static SettlementEntity toEntity(Settlement settlement) {
        return new SettlementEntity(
                settlement.id().id(),
                settlement.groupId().id(),
                settlement.payerId().id(),
                settlement.payeeId().id(),
                settlement.amount().value(),
                settlement.createdAt()
        );
    }
}
