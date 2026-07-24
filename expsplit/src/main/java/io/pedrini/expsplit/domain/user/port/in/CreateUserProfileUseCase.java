package io.pedrini.expsplit.domain.user.port.in;

import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileEmail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface CreateUserProfileUseCase {

    UserProfile create(UserProfileId id, UserProfileEmail email);
}
