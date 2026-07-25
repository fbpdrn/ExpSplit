package io.pedrini.expsplit.adapters.out.persistence.settlement;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class SettlementRepository implements io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository {

    private final SettlementJpaRepository jpaRepository;

    SettlementRepository(SettlementJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Settlement save(Settlement settlement) {
        SettlementEntity saved = jpaRepository.save(SettlementMapper.toEntity(settlement));
        return SettlementMapper.toDomain(saved);
    }

    @Override
    public Optional<Settlement> findById(SettlementId id) {
        return jpaRepository.findById(id.id()).map(SettlementMapper::toDomain);
    }

    @Override
    public List<Settlement> findByGroupId(GroupId groupId) {
        return jpaRepository.findByGroupId(groupId.id()).stream().map(SettlementMapper::toDomain).toList();
    }

    @Override
    public void deleteById(SettlementId id) {
        jpaRepository.deleteById(id.id());
    }
}
