package io.pedrini.auth.application;

import io.pedrini.auth.domain.user.exception.InvalidCredentialsException;
import io.pedrini.auth.domain.user.exception.UserAuthLockedException;
import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.port.in.AuthenticateUserUseCase;
import io.pedrini.auth.domain.user.port.out.PasswordHasher;
import io.pedrini.auth.domain.user.port.out.TokenIssuer;
import io.pedrini.auth.domain.user.port.out.UserAuthRepository;

public class AuthenticationService implements AuthenticateUserUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PasswordHasher passwordHasher;
    private final TokenIssuer tokenIssuer;

    public AuthenticationService(UserAuthRepository userAuthRepository, PasswordHasher passwordHasher, TokenIssuer tokenIssuer) {
        this.userAuthRepository = userAuthRepository;
        this.passwordHasher = passwordHasher;
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    public String authenticate(UserAuthEmail email, String rawPassword) {
        UserAuth userAuth = userAuthRepository.findByEmail(email)
                .orElseThrow(InvalidCredentialsException::new);

        if (userAuth.isLocked()) {
            throw new UserAuthLockedException(userAuth.id());
        }

        if (!passwordHasher.matches(rawPassword, userAuth.password())) {
            userAuth.loginFailed();
            userAuthRepository.save(userAuth);
            throw new InvalidCredentialsException();
        }

        userAuth.loginSuccessful();
        userAuthRepository.save(userAuth);

        return tokenIssuer.issue(userAuth.id(), userAuth.email());
    }
}
