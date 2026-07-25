package io.pedrini.expsplit.adapters.out.persistence.settlement;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

interface SettlementJpaRepository extends JpaRepository<SettlementEntity, UUID> {

    List<SettlementEntity> findByGroupId(UUID groupId);
}
