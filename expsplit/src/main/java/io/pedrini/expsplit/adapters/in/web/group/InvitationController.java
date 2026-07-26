package io.pedrini.expsplit.adapters.in.web.group;

import io.pedrini.expsplit.adapters.in.web.group.dto.PendingInvitationResponse;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.Membership;
import io.pedrini.expsplit.domain.group.port.in.ListPendingInvitationsUseCase;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/invitations")
public class InvitationController {

    private final ListPendingInvitationsUseCase listPendingInvitationsUseCase;

    public InvitationController(ListPendingInvitationsUseCase listPendingInvitationsUseCase) {
        this.listPendingInvitationsUseCase = listPendingInvitationsUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PendingInvitationResponse>> list(@AuthenticationPrincipal Jwt jwt) {
        UserProfileId userId = userId(jwt);
        List<PendingInvitationResponse> invitations = listPendingInvitationsUseCase.list(userId).stream()
                .map(group -> PendingInvitationResponse.from(group, pendingMembership(group, userId)))
                .toList();
        return ResponseEntity.ok(invitations);
    }

    private Membership pendingMembership(Group group, UserProfileId userId) {
        return group.members().stream()
                .filter(member -> member.userId().equals(userId) && member.isPending())
                .findFirst()
                .orElseThrow();
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }
}
