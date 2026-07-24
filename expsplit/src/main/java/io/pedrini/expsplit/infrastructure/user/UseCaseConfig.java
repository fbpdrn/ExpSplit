package io.pedrini.expsplit.infrastructure.user;

import io.pedrini.expsplit.application.user.UserProfileService;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UseCaseConfig {

    @Bean
    public UserProfileService userProfileService(UserProfileRepository userProfileRepository) {
        return new UserProfileService(userProfileRepository);
    }
}
