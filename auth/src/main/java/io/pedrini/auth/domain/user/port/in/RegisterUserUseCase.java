package io.pedrini.auth.domain.user.port.in;

import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;

public interface RegisterUserUseCase {

    UserAuthId register(UserAuthEmail email, String rawPassword);
}
