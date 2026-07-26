package io.pedrini.expsplit.adapters.in.web.group;

import io.pedrini.expsplit.adapters.in.web.group.dto.CreateGroupRequest;
import io.pedrini.expsplit.adapters.in.web.group.dto.GroupResponse;
import io.pedrini.expsplit.adapters.in.web.group.dto.InviteMemberRequest;
import io.pedrini.expsplit.adapters.in.web.user.UserProfileResolver;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.Membership;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.model.GroupName;
import io.pedrini.expsplit.domain.group.port.in.AcceptInvitationUseCase;
import io.pedrini.expsplit.domain.group.port.in.CreateGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.DeleteGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.GetGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.InviteMemberUseCase;
import io.pedrini.expsplit.domain.group.port.in.LeaveGroupUseCase;
import io.pedrini.expsplit.domain.group.port.in.RejectInvitationUseCase;
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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/groups")
public class GroupController {

    private final CreateGroupUseCase createGroupUseCase;
    private final GetGroupUseCase getGroupUseCase;
    private final InviteMemberUseCase inviteMemberUseCase;
    private final AcceptInvitationUseCase acceptInvitationUseCase;
    private final RejectInvitationUseCase rejectInvitationUseCase;
    private final LeaveGroupUseCase leaveGroupUseCase;
    private final DeleteGroupUseCase deleteGroupUseCase;
    private final UserProfileResolver userProfileResolver;

    public GroupController(CreateGroupUseCase createGroupUseCase,
                            GetGroupUseCase getGroupUseCase,
                            InviteMemberUseCase inviteMemberUseCase,
                            AcceptInvitationUseCase acceptInvitationUseCase,
                            RejectInvitationUseCase rejectInvitationUseCase,
                            LeaveGroupUseCase leaveGroupUseCase,
                            DeleteGroupUseCase deleteGroupUseCase,
                            UserProfileResolver userProfileResolver) {
        this.createGroupUseCase = createGroupUseCase;
        this.getGroupUseCase = getGroupUseCase;
        this.inviteMemberUseCase = inviteMemberUseCase;
        this.acceptInvitationUseCase = acceptInvitationUseCase;
        this.rejectInvitationUseCase = rejectInvitationUseCase;
        this.leaveGroupUseCase = leaveGroupUseCase;
        this.deleteGroupUseCase = deleteGroupUseCase;
        this.userProfileResolver = userProfileResolver;
    }

    @PostMapping
    public ResponseEntity<GroupResponse> create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateGroupRequest request) {
        Group group = createGroupUseCase.create(new GroupName(request.name()), userId(jwt));
        return ResponseEntity.ok(toResponse(group));
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<GroupResponse> get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        Group group = getGroupUseCase.get(new GroupId(groupId), userId(jwt));
        return ResponseEntity.ok(toResponse(group));
    }

    @PostMapping("/{groupId}/invitations")
    public ResponseEntity<GroupResponse> invite(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId, @Valid @RequestBody InviteMemberRequest request) {
        Group group = inviteMemberUseCase.invite(new GroupId(groupId), userId(jwt), new UserProfileId(request.userId()));
        return ResponseEntity.ok(toResponse(group));
    }

    @PostMapping("/{groupId}/invitations/accept")
    public ResponseEntity<GroupResponse> accept(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        Group group = acceptInvitationUseCase.accept(new GroupId(groupId), userId(jwt));
        return ResponseEntity.ok(toResponse(group));
    }

    @PostMapping("/{groupId}/invitations/reject")
    public ResponseEntity<GroupResponse> reject(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        Group group = rejectInvitationUseCase.reject(new GroupId(groupId), userId(jwt));
        return ResponseEntity.ok(toResponse(group));
    }

    @DeleteMapping("/{groupId}/members/me")
    public ResponseEntity<Void> leave(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        leaveGroupUseCase.leave(new GroupId(groupId), userId(jwt));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{groupId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID groupId) {
        deleteGroupUseCase.delete(new GroupId(groupId), userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private UserProfileId userId(Jwt jwt) {
        return new UserProfileId(UUID.fromString(Objects.requireNonNull(jwt.getSubject())));
    }

    private GroupResponse toResponse(Group group) {
        List<UserProfileId> memberIds = group.members().stream().map(Membership::userId).toList();
        return GroupResponse.from(group, userProfileResolver.resolve(memberIds));
    }
}
