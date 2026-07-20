package io.pedrini.auth.domain.user.port.out;

import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;

import java.util.Optional;

public interface UserAuthRepository {

    UserAuth save(UserAuth userAuth);

    Optional<UserAuth> findById(UserAuthId id);

    Optional<UserAuth> findByEmail(UserAuthEmail email);

    boolean existsByEmail(UserAuthEmail email);
}
