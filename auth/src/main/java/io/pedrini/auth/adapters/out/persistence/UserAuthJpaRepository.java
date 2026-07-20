package io.pedrini.auth.adapters.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface UserAuthJpaRepository extends JpaRepository<UserAuthEntity, UUID> {

    Optional<UserAuthEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
