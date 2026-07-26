package io.pedrini.expsplit.adapters.in.web.settlement;

import io.pedrini.expsplit.adapters.in.web.settlement.dto.CreateSettlementRequest;
import io.pedrini.expsplit.adapters.in.web.settlement.dto.SettlementResponse;
import io.pedrini.expsplit.adapters.in.web.user.UserProfileResolver;
import io.pedrini.expsplit.adapters.in.web.user.dto.UserProfileResponse;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.settlement.port.in.CreateSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.DeleteSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.GetSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.ListSettlementsUseCase;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/groups/{groupId}/settlements")
public class SettlementController {

    private final CreateSettlementUseCase createSettlementUseCase;
    private final GetSettlementUseCase getSettlementUseCase;
    private final ListSettlementsUseCase listSettlementsUseCase;
    private final DeleteSettlementUseCase deleteSettlementUseCase;
    private final UserProfileResolver userProfileResolver;

    public SettlementController(CreateSettlementUseCase createSettlementUseCase,
                                 GetSettlementUseCase getSettlementUseCase,
                                 ListSettlementsUseCase listSettlementsUseCase,
                                 DeleteSettlementUseCase deleteSettlementUseCase,
                                 UserProfileResolver userProfileResolver) {
        this.createSettlementUseCase = createSettlementUseCase;
        this.getSettlementUseCase = getSettlementUseCase;
        this.listSettlementsUseCase = listSettlementsUseCase;
        this.deleteSettlementUseCase = deleteSettlementUseCase;
        this.userProfileResolver = userProfileResolver;
    }

    @PostMapping
    public ResponseEntity<SettlementResponse> create(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                                       @Valid @RequestBody CreateSettlementRequest request) {
        Settlement settlement = createSettlementUseCase.create(
                new GroupId(groupId), userId(jwt), new UserProfileId(request.payeeId()), new Amount(request.amount()), request.category());
        return ResponseEntity.ok(toResponse(settlement));
    }

    @GetMapping
    public ResponseEntity<List<SettlementResponse>> list(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        List<Settlement> settlements = listSettlementsUseCase.list(new GroupId(groupId), userId(jwt));
        Map<UserProfileId, UserProfileResponse> users = userProfileResolver.resolve(userIds(settlements));
        return ResponseEntity.ok(settlements.stream().map(settlement -> SettlementResponse.from(settlement, users)).toList());
    }

    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                                    @PathVariable UUID settlementId) {
        Settlement settlement = getSettlementUseCase.get(new GroupId(groupId), new SettlementId(settlementId), userId(jwt));
        return ResponseEntity.ok(toResponse(settlement));
    }

    @DeleteMapping("/{settlementId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId,
                                        @PathVariable UUID settlementId) {
        deleteSettlementUseCase.delete(new GroupId(groupId), new SettlementId(settlementId), userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }

    private SettlementResponse toResponse(Settlement settlement) {
        return SettlementResponse.from(settlement, userProfileResolver.resolve(userIds(List.of(settlement))));
    }

    private Set<UserProfileId> userIds(List<Settlement> settlements) {
        Set<UserProfileId> ids = new HashSet<>();
        for (Settlement settlement : settlements) {
            ids.add(settlement.payerId());
            ids.add(settlement.payeeId());
        }
        return ids;
    }
}
