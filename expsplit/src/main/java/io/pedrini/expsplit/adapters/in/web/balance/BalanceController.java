package io.pedrini.expsplit.adapters.in.web.balance;

import io.pedrini.expsplit.adapters.in.web.balance.dto.BalanceResponse;
import io.pedrini.expsplit.adapters.in.web.balance.dto.UserBalanceResponse;
import io.pedrini.expsplit.adapters.in.web.user.UserProfileResolver;
import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.balance.model.Balance;
import io.pedrini.expsplit.domain.balance.model.UserBalanceDetail;
import io.pedrini.expsplit.domain.balance.port.in.GetGroupBalanceUseCase;
import io.pedrini.expsplit.domain.balance.port.in.GetUserBalanceUseCase;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/balance")
public class BalanceController {

    private final GetGroupBalanceUseCase getGroupBalanceUseCase;
    private final GetUserBalanceUseCase getUserBalanceUseCase;
    private final UserProfileResolver userProfileResolver;

    public BalanceController(GetGroupBalanceUseCase getGroupBalanceUseCase, GetUserBalanceUseCase getUserBalanceUseCase,
                              UserProfileResolver userProfileResolver) {
        this.getGroupBalanceUseCase = getGroupBalanceUseCase;
        this.getUserBalanceUseCase = getUserBalanceUseCase;
        this.userProfileResolver = userProfileResolver;
    }

    @GetMapping
    public ResponseEntity<List<BalanceResponse>> getGroupBalance(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        List<Balance> balances = getGroupBalanceUseCase.getGroupBalance(new GroupId(groupId), userId(jwt));

        Set<UserProfileId> ids = new HashSet<>();
        balances.forEach(balance -> ids.add(balance.userId()));
        Map<UserProfileId, UserProfileResponse> users = userProfileResolver.resolve(ids);

        return ResponseEntity.ok(balances.stream().map(balance -> BalanceResponse.from(balance, users)).toList());
    }

    @GetMapping("/me")
    public ResponseEntity<UserBalanceResponse> getMyBalance(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UserBalanceDetail detail = getUserBalanceUseCase.getUserBalance(new GroupId(groupId), userId(jwt));

        Set<UserProfileId> ids = new HashSet<>();
        ids.add(detail.userId());
        detail.perCounterpart().forEach(comparison -> ids.add(comparison.counterpartId()));
        Map<UserProfileId, UserProfileResponse> users = userProfileResolver.resolve(ids);

        return ResponseEntity.ok(UserBalanceResponse.from(detail, users));
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }
}
