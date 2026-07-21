package io.pedrini.auth.domain.user.port.out;

import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;

public interface TokenIssuer {

    String issue(UserAuthId userId, UserAuthEmail email);
}
