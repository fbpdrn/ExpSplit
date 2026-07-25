package io.pedrini.expsplit.adapters.out.persistence.group;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface GroupJpaRepository extends JpaRepository<GroupEntity, UUID> {
}
