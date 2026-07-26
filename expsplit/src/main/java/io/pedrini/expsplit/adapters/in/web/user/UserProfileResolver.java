package io.pedrini.expsplit.adapters.in.web.user;

import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UserProfileResolver {

    private final UserProfileRepository userProfileRepository;

    public UserProfileResolver(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    public Map<UserProfileId, UserProfileResponse> resolve(Collection<UserProfileId> ids) {
        return userProfileRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(UserProfile::id, UserProfileResponse::from));
    }
}
