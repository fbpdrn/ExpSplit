package io.pedrini.expsplit.application.user;

import io.pedrini.expsplit.domain.user.exception.UserProfileAlreadyExistsException;
import io.pedrini.expsplit.domain.user.exception.UserProfileNotFoundException;
import io.pedrini.expsplit.domain.user.model.FirstName;
import io.pedrini.expsplit.domain.user.model.LastName;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileEmail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import io.pedrini.expsplit.domain.user.port.in.CreateUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.DeleteUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.GetUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.UpdateUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;

public class UserProfileService implements GetUserProfileUseCase, CreateUserProfileUseCase, UpdateUserProfileUseCase, DeleteUserProfileUseCase {

    private final UserProfileRepository userProfileRepository;

    public UserProfileService(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfile get(UserProfileId id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new UserProfileNotFoundException(id));
    }

    @Override
    public UserProfile create(UserProfileId id, UserProfileEmail email) {
        if (userProfileRepository.findById(id).isPresent()) {
            throw new UserProfileAlreadyExistsException(id);
        }
        return userProfileRepository.save(UserProfile.create(id, email));
    }

    @Override
    public UserProfile update(UserProfileId id, FirstName firstName, LastName lastName) {
        UserProfile userProfile = userProfileRepository.findById(id)
                .orElseThrow(() -> new UserProfileNotFoundException(id));

        userProfile.updateName(firstName, lastName);
        return userProfileRepository.save(userProfile);
    }

    @Override
    public void delete(UserProfileId id) {
        if (userProfileRepository.findById(id).isEmpty()) {
            throw new UserProfileNotFoundException(id);
        }
        userProfileRepository.deleteById(id);
    }
}
