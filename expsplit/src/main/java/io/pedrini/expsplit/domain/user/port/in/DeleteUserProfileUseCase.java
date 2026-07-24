package io.pedrini.expsplit.domain.user.port.in;

import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface DeleteUserProfileUseCase {

    void delete(UserProfileId id);
}
