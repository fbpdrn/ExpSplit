package io.pedrini.expsplit.adapters.out.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

interface GroupJpaRepository extends JpaRepository<GroupEntity, UUID> {

    @Query("SELECT DISTINCT g FROM GroupEntity g JOIN g.members m WHERE m.userId = :userId AND m.status = 'PENDING'")
    List<GroupEntity> findPendingInvitations(@Param("userId") UUID userId);
}
