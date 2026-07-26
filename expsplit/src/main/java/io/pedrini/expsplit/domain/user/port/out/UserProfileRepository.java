package io.pedrini.expsplit.domain.user.port.out;

import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserProfileRepository {

    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findById(UserProfileId id);

    List<UserProfile> findAllById(Collection<UserProfileId> ids);

    void deleteById(UserProfileId id);
}
