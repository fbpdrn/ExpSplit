package io.pedrini.expsplit.infrastructure.group;

import io.pedrini.expsplit.application.group.GroupService;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GroupUseCaseConfig {

    @Bean
    public GroupService groupService(GroupRepository groupRepository, UserProfileRepository userProfileRepository) {
        return new GroupService(groupRepository, userProfileRepository);
    }
}
