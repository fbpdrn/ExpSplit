package io.pedrini.auth.adapters.out.persistence;

import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
class UserAuthRepository implements io.pedrini.auth.domain.user.port.out.UserAuthRepository {

    private final UserAuthJpaRepository jpaRepository;

    UserAuthRepository(UserAuthJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserAuth save(UserAuth userAuth) {
        UserAuthEntity saved = jpaRepository.save(UserAuthMapper.toEntity(userAuth));
        return UserAuthMapper.toDomain(saved);
    }

    @Override
    public Optional<UserAuth> findById(UserAuthId id) {
        return jpaRepository.findById(id.id()).map(UserAuthMapper::toDomain);
    }

    @Override
    public Optional<UserAuth> findByEmail(UserAuthEmail email) {
        return jpaRepository.findByEmail(email.email()).map(UserAuthMapper::toDomain);
    }

    @Override
    public boolean existsByEmail(UserAuthEmail email) {
        return jpaRepository.existsByEmail(email.email());
    }
}
