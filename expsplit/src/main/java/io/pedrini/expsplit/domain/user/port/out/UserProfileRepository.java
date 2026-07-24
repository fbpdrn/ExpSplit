package io.pedrini.expsplit.domain.user.port.out;

import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.Optional;

public interface UserProfileRepository {

    UserProfile save(UserProfile userProfile);

    Optional<UserProfile> findById(UserProfileId id);

    void deleteById(UserProfileId id);
}
