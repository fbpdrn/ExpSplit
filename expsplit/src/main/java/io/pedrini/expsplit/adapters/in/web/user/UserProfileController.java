package io.pedrini.expsplit.adapters.in.web.user;

import io.pedrini.expsplit.adapters.in.web.user.dto.UpdateUserProfileRequest;
import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.user.model.FirstName;
import io.pedrini.expsplit.domain.user.model.LastName;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileEmail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import io.pedrini.expsplit.domain.user.port.in.CreateUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.DeleteUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.GetUserProfileUseCase;
import io.pedrini.expsplit.domain.user.port.in.UpdateUserProfileUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/profile")
public class UserProfileController {

    private static final String EMAIL_CLAIM = "email";

    private final GetUserProfileUseCase getUserProfileUseCase;
    private final CreateUserProfileUseCase createUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
    private final DeleteUserProfileUseCase deleteUserProfileUseCase;

    public UserProfileController(GetUserProfileUseCase getUserProfileUseCase,
                                  CreateUserProfileUseCase createUserProfileUseCase,
                                  UpdateUserProfileUseCase updateUserProfileUseCase,
                                  DeleteUserProfileUseCase deleteUserProfileUseCase) {
        this.getUserProfileUseCase = getUserProfileUseCase;
        this.createUserProfileUseCase = createUserProfileUseCase;
        this.updateUserProfileUseCase = updateUserProfileUseCase;
        this.deleteUserProfileUseCase = deleteUserProfileUseCase;
    }

    @PostMapping
    public ResponseEntity<UserProfileResponse> create(@AuthenticationPrincipal Jwt jwt) {
        UserProfile userProfile = createUserProfileUseCase.create(id(jwt), email(jwt));
        return ResponseEntity.ok(UserProfileResponse.from(userProfile));
    }

    @GetMapping
    public ResponseEntity<UserProfileResponse> get(@AuthenticationPrincipal Jwt jwt) {
        UserProfile userProfile = getUserProfileUseCase.get(id(jwt));
        return ResponseEntity.ok(UserProfileResponse.from(userProfile));
    }

    @PatchMapping
    public ResponseEntity<UserProfileResponse> update(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody UpdateUserProfileRequest request) {
        UserProfile userProfile = updateUserProfileUseCase.update(id(jwt), new FirstName(request.firstName()), new LastName(request.lastName()));
        return ResponseEntity.ok(UserProfileResponse.from(userProfile));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt) {
        deleteUserProfileUseCase.delete(id(jwt));
        return ResponseEntity.noContent().build();
    }

    private UserProfileId id(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }

    private UserProfileEmail email(Jwt jwt) {
        return new UserProfileEmail(Objects.requireNonNull(jwt.getClaimAsString(EMAIL_CLAIM)));
    }
}
