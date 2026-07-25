package io.pedrini.expsplit.adapters.in.web.statistics;

import io.pedrini.expsplit.adapters.in.web.statistics.dto.CategoryStatisticResponse;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.statistics.port.in.GetGroupStatisticsUseCase;
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
@RequestMapping("/groups/{groupId}/statistics")
public class StatisticsController {

    private final GetGroupStatisticsUseCase getGroupStatisticsUseCase;

    public StatisticsController(GetGroupStatisticsUseCase getGroupStatisticsUseCase) {
        this.getGroupStatisticsUseCase = getGroupStatisticsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CategoryStatisticResponse>> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        List<CategoryStatisticResponse> statistics = getGroupStatisticsUseCase.getStatistics(new GroupId(groupId), userId(jwt)).stream()
                .map(CategoryStatisticResponse::from)
                .toList();
        return ResponseEntity.ok(statistics);
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }
}
