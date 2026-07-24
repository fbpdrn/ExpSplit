package io.pedrini.expsplit.adapters.out.persistence.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

interface UserProfileJpaRepository extends JpaRepository<UserProfileEntity, UUID> {
}
