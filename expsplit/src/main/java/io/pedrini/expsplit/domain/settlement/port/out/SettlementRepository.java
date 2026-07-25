package io.pedrini.expsplit.domain.settlement.port.out;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository {

    Settlement save(Settlement settlement);

    Optional<Settlement> findById(SettlementId id);

    List<Settlement> findByGroupId(GroupId groupId);

    void deleteById(SettlementId id);
}
