package io.pedrini.auth.application;

import io.pedrini.auth.domain.user.exception.EmailAlreadyRegisteredException;
import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthId;
import io.pedrini.auth.domain.user.port.in.RegisterUserUseCase;
import io.pedrini.auth.domain.user.port.out.PasswordHasher;
import io.pedrini.auth.domain.user.port.out.UserAuthRepository;

public class RegistrationService implements RegisterUserUseCase {

    private final UserAuthRepository userAuthRepository;
    private final PasswordHasher passwordHasher;

    public RegistrationService(UserAuthRepository userAuthRepository, PasswordHasher passwordHasher) {
        this.userAuthRepository = userAuthRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public UserAuthId register(UserAuthEmail email, String rawPassword) {
        if (userAuthRepository.existsByEmail(email)) {
            throw new EmailAlreadyRegisteredException(email);
        }

        UserAuth userAuth = UserAuth.register(email, passwordHasher.hash(rawPassword));
        return userAuthRepository.save(userAuth).id();
    }
}
