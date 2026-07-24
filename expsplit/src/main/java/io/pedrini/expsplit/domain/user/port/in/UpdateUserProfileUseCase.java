package io.pedrini.expsplit.domain.user.port.in;

import io.pedrini.expsplit.domain.user.model.FirstName;
import io.pedrini.expsplit.domain.user.model.LastName;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface UpdateUserProfileUseCase {

    UserProfile update(UserProfileId id, FirstName firstName, LastName lastName);
}
