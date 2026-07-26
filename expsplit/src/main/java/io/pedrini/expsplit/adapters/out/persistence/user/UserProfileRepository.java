package io.pedrini.expsplit.adapters.out.persistence.user;

import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
class UserProfileRepository implements io.pedrini.expsplit.domain.user.port.out.UserProfileRepository {

    private final UserProfileJpaRepository jpaRepository;

    UserProfileRepository(UserProfileJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public UserProfile save(UserProfile userProfile) {
        UserProfileEntity saved = jpaRepository.save(UserProfileMapper.toEntity(userProfile));
        return UserProfileMapper.toDomain(saved);
    }

    @Override
    public Optional<UserProfile> findById(UserProfileId id) {
        return jpaRepository.findById(id.id()).map(UserProfileMapper::toDomain);
    }

    @Override
    public List<UserProfile> findAllById(Collection<UserProfileId> ids) {
        List<UUID> uuids = ids.stream().map(UserProfileId::id).toList();
        return jpaRepository.findAllById(uuids).stream().map(UserProfileMapper::toDomain).toList();
    }

    @Override
    public void deleteById(UserProfileId id) {
        jpaRepository.deleteById(id.id());
    }
}
