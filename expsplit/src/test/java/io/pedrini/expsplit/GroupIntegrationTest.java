package io.pedrini.expsplit;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.user.model.UserProfile;
import io.pedrini.expsplit.domain.user.model.UserProfileEmail;
import io.pedrini.expsplit.domain.user.model.UserProfileId;
import io.pedrini.expsplit.domain.user.port.out.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupIntegrationTest {

    private static final String GROUP = "Group";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private GroupRepository groupRepository;

    private static RequestPostProcessor authenticatedAs(UUID userId) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(builder -> builder.subject(userId.toString()).claim("email", userId + "@example.com"));
    }

    private void createProfile(UUID userId) {
        userProfileRepository.save(UserProfile.create(new UserProfileId(userId), new UserProfileEmail(userId + "@example.com")));
    }

    private UUID createGroup(UUID ownerId) throws Exception {
        String body = mockMvc.perform(post("/groups")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGroupRequest(GROUP))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return UUID.fromString(json.get("id").asString());
    }

    private ResultActions invite(UUID groupId, UUID inviterId, UUID inviteeId) throws Exception {
        return mockMvc.perform(post("/groups/" + groupId + "/invitations")
                .with(authenticatedAs(inviterId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new InviteMemberRequest(inviteeId))));
    }

    private ResultActions accept(UUID groupId, UUID userId) throws Exception {
        return mockMvc.perform(post("/groups/" + groupId + "/invitations/accept").with(authenticatedAs(userId)));
    }

    private ResultActions reject(UUID groupId, UUID userId) throws Exception {
        return mockMvc.perform(post("/groups/" + groupId + "/invitations/reject").with(authenticatedAs(userId)));
    }

    private ResultActions leave(UUID groupId, UUID userId) throws Exception {
        return mockMvc.perform(delete("/groups/" + groupId + "/members/me").with(authenticatedAs(userId)));
    }

    private ResultActions deleteGroup(UUID groupId, UUID userId) throws Exception {
        return mockMvc.perform(delete("/groups/" + groupId).with(authenticatedAs(userId)));
    }

    private ResultActions getGroup(UUID groupId, UUID userId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId).with(authenticatedAs(userId)));
    }

    private ResultActions listInvitations(UUID userId) throws Exception {
        return mockMvc.perform(get("/invitations").with(authenticatedAs(userId)));
    }

    private JsonNode memberNode(ResultActions result, UUID userId) throws Exception {
        JsonNode json = objectMapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
        for (JsonNode member : json.get("members")) {
            if (member.get("user").get("id").asString().equals(userId.toString())) {
                return member;
            }
        }
        throw new AssertionError("Member not found in response: " + userId);
    }

    @Test
    void createGroup() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);

        UUID groupId = createGroup(ownerId);

        getGroup(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(GROUP))
                .andExpect(jsonPath("$.members.length()").value(1))
                .andExpect(jsonPath("$.members[0].user.id").value(ownerId.toString()))
                .andExpect(jsonPath("$.members[0].role").value("OWNER"))
                .andExpect(jsonPath("$.members[0].status").value("ACCEPTED"));
    }

    @Test
    void createGroupBlank() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);

        mockMvc.perform(post("/groups")
                        .with(authenticatedAs(ownerId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateGroupRequest(" "))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void groupNotFound() throws Exception {
        UUID userId = UUID.randomUUID();

        getGroup(UUID.randomUUID(), userId).andExpect(status().isNotFound());
    }

    @Test
    void groupNotMember() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID randomId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getGroup(groupId, randomId).andExpect(status().isNotFound());
    }

    @Test
    void invitePending() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);

        invite(groupId, ownerId, memberId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(2));
    }

    @Test
    void invitePermission() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        createProfile(strangerId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());
        accept(groupId, memberId).andExpect(status().isOk());

        invite(groupId, memberId, strangerId).andExpect(status().isForbidden());
    }

    @Test
    void inviteDuplicate() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);

        invite(groupId, ownerId, memberId).andExpect(status().isOk());
        invite(groupId, ownerId, memberId).andExpect(status().isConflict());
    }

    @Test
    void inviteNotMember() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID randomId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        invite(groupId, ownerId, randomId).andExpect(status().isNotFound());
    }

    @Test
    void inviteAccept() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());

        ResultActions result = accept(groupId, memberId).andExpect(status().isOk());

        assertThat(memberNode(result, memberId).get("status").asString()).isEqualTo("ACCEPTED");
    }

    @Test
    void inviteAcceptNoInvite() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        accept(groupId, memberId).andExpect(status().isNotFound());
    }

    @Test
    void inviteReject() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());

        reject(groupId, memberId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.members.length()").value(1));
    }

    @Test
    void listPendingInvitations() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);

        invite(groupId, ownerId, memberId).andExpect(status().isOk());

        listInvitations(memberId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].groupId").value(groupId.toString()))
                .andExpect(jsonPath("$[0].groupName").value(GROUP));
    }

    @Test
    void listPendingInvitationsEmpty() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId);

        listInvitations(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listPendingInvitationsFilterResponse() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());

        accept(groupId, memberId).andExpect(status().isOk());

        listInvitations(memberId).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
        listInvitations(ownerId).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void leaveGroupExists() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());
        accept(groupId, memberId).andExpect(status().isOk());

        leave(groupId, memberId).andExpect(status().isNoContent());

        assertThat(groupRepository.findById(new GroupId(groupId))).isPresent();
        getGroup(groupId, ownerId).andExpect(status().isOk()).andExpect(jsonPath("$.members.length()").value(1));
    }

    @Test
    void leaveGroupDelete() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        leave(groupId, ownerId).andExpect(status().isNoContent());

        assertThat(groupRepository.findById(new GroupId(groupId))).isEmpty();
        getGroup(groupId, ownerId).andExpect(status().isNotFound());
    }

    @Test
    void leaveGroupPromote() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());
        accept(groupId, memberId).andExpect(status().isOk());

        leave(groupId, ownerId).andExpect(status().isNoContent());

        assertThat(groupRepository.findById(new GroupId(groupId))).isPresent();
        ResultActions result = getGroup(groupId, memberId).andExpect(status().isOk());

        assertThat(memberNode(result, memberId).get("role").asString()).isEqualTo("OWNER");
    }

    @Test
    void leaveGroupNoMember() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID randomId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        leave(groupId, randomId).andExpect(status().isNotFound());
    }

    @Test
    void deleteGroup() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        deleteGroup(groupId, ownerId).andExpect(status().isNoContent());

        assertThat(groupRepository.findById(new GroupId(groupId))).isEmpty();
    }

    @Test
    void deleteGroupNoPermission() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        createProfile(memberId);
        UUID groupId = createGroup(ownerId);
        invite(groupId, ownerId, memberId).andExpect(status().isOk());
        accept(groupId, memberId).andExpect(status().isOk());

        deleteGroup(groupId, memberId).andExpect(status().isForbidden());
    }

    @Test
    void deleteGroupNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        deleteGroup(UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    private record CreateGroupRequest(String name) { }

    private record InviteMemberRequest(UUID userId) { }
}
