package io.pedrini.auth.infrastructure;

import io.pedrini.auth.application.AuthenticationService;
import io.pedrini.auth.application.RegistrationService;
import io.pedrini.auth.domain.user.port.in.AuthenticateUserUseCase;
import io.pedrini.auth.domain.user.port.in.RegisterUserUseCase;
import io.pedrini.auth.domain.user.port.out.PasswordHasher;
import io.pedrini.auth.domain.user.port.out.TokenIssuer;
import io.pedrini.auth.domain.user.port.out.UserAuthRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public RegisterUserUseCase registerUserUseCase(UserAuthRepository userAuthRepository, PasswordHasher passwordHasher) {
        return new RegistrationService(userAuthRepository, passwordHasher);
    }

    @Bean
    public AuthenticateUserUseCase authenticateUserUseCase(UserAuthRepository userAuthRepository,
                                                            PasswordHasher passwordHasher,
                                                            TokenIssuer tokenIssuer) {
        return new AuthenticationService(userAuthRepository, passwordHasher, tokenIssuer);
    }
}
