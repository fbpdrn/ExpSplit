package io.pedrini.expsplit.adapters.out.persistence.user;

import io.pedrini.expsplit.domain.user.model.FirstName;
import io.pedrini.expsplit.domain.user.model.LastName;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileEmail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

class UserProfileMapper {

    static UserProfile toDomain(UserProfileEntity entity) {
        return new UserProfile(
                new UserProfileId(entity.getId()),
                new UserProfileEmail(entity.getEmail()),
                entity.getFirstName() != null ? new FirstName(entity.getFirstName()) : null,
                entity.getLastName() != null ? new LastName(entity.getLastName()) : null,
                entity.getCreatedAt()
        );
    }

    static UserProfileEntity toEntity(UserProfile userProfile) {
        return new UserProfileEntity(
                userProfile.id().id(),
                userProfile.email().email(),
                userProfile.firstName() != null ? userProfile.firstName().value() : null,
                userProfile.lastName() != null ? userProfile.lastName().value() : null,
                userProfile.createdAt()
        );
    }
}
