package io.pedrini.auth.adapters.out.persistence;

import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;
import io.pedrini.auth.domain.user.model.UserAuthPassword;

class UserAuthMapper {

    static UserAuth toDomain(UserAuthEntity entity) {
        return new UserAuth(
                new UserAuthId(entity.getId()),
                new UserAuthEmail(entity.getEmail()),
                new UserAuthPassword(entity.getPasswordHash(), entity.getPasswordAlgorithm()),
                entity.getStatus(),
                entity.getFailedAttempts(),
                entity.getLockedUntil(),
                entity.getCreatedAt()
        );
    }

    static UserAuthEntity toEntity(UserAuth userAuth) {
        return new UserAuthEntity(
                userAuth.id().id(),
                userAuth.email().email(),
                userAuth.password().hash(),
                userAuth.password().algorithm(),
                userAuth.status(),
                userAuth.failedAttempts(),
                userAuth.lockedUntil(),
                userAuth.createdAt()
        );
    }
}
