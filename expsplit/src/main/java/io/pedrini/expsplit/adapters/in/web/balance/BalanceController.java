package io.pedrini.expsplit.adapters.in.web.balance;

import io.pedrini.expsplit.adapters.in.web.balance.dto.BalanceResponse;
import io.pedrini.expsplit.adapters.in.web.balance.dto.UserBalanceResponse;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/balance")
public class BalanceController {

    private final GetGroupBalanceUseCase getGroupBalanceUseCase;
    private final GetUserBalanceUseCase getUserBalanceUseCase;

    public BalanceController(GetGroupBalanceUseCase getGroupBalanceUseCase, GetUserBalanceUseCase getUserBalanceUseCase) {
        this.getGroupBalanceUseCase = getGroupBalanceUseCase;
        this.getUserBalanceUseCase = getUserBalanceUseCase;
    }

    @GetMapping
    public ResponseEntity<List<BalanceResponse>> getGroupBalance(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        List<BalanceResponse> balances = getGroupBalanceUseCase.getGroupBalance(new GroupId(groupId), userId(jwt)).stream()
                .map(BalanceResponse::from)
                .toList();
        return ResponseEntity.ok(balances);
    }

    @GetMapping("/me")
    public ResponseEntity<UserBalanceResponse> getMyBalance(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        UserBalanceResponse balance = UserBalanceResponse.from(getUserBalanceUseCase.getUserBalance(new GroupId(groupId), userId(jwt)));
        return ResponseEntity.ok(balance);
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }
}
