package io.pedrini.auth.domain.user.port.in;

import io.pedrini.auth.domain.user.model.UserAuthEmail;

public interface AuthenticateUserUseCase {

    String authenticate(UserAuthEmail email, String rawPassword);
}
