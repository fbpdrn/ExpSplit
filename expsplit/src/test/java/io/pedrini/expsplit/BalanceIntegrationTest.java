package io.pedrini.expsplit;

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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BalanceIntegrationTest {

    private static final String GROUP = "Group";
    private static final String DESCRIPTION = "Dinner";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

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
        return UUID.fromString(objectMapper.readTree(body).get("id").asString());
    }

    private void invite(UUID groupId, UUID inviterId, UUID inviteeId) throws Exception {
        mockMvc.perform(post("/groups/" + groupId + "/invitations")
                        .with(authenticatedAs(inviterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new InviteMemberRequest(inviteeId))))
                .andExpect(status().isOk());
    }

    private void accept(UUID groupId, UUID userId) throws Exception {
        mockMvc.perform(post("/groups/" + groupId + "/invitations/accept").with(authenticatedAs(userId)))
                .andExpect(status().isOk());
    }

    private void addAcceptedMember(UUID groupId, UUID ownerId, UUID memberId) throws Exception {
        createProfile(memberId);
        invite(groupId, ownerId, memberId);
        accept(groupId, memberId);
    }

    private void createTransaction(UUID groupId, UUID requesterId, String amount, List<ShareRequest> shares) throws Exception {
        mockMvc.perform(post("/groups/" + groupId + "/transactions")
                        .with(authenticatedAs(requesterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTransactionRequest(DESCRIPTION, new BigDecimal(amount), shares))))
                .andExpect(status().isOk());
    }

    private void createSettlement(UUID groupId, UUID requesterId, UUID payeeId, String amount) throws Exception {
        mockMvc.perform(post("/groups/" + groupId + "/settlements")
                        .with(authenticatedAs(requesterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateSettlementRequest(payeeId, new BigDecimal(amount)))))
                .andExpect(status().isOk());
    }

    private ResultActions getGroupBalance(UUID groupId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/balance").with(authenticatedAs(requesterId)));
    }

    private ResultActions getMyBalance(UUID groupId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/balance/me").with(authenticatedAs(requesterId)));
    }

    private JsonNode balanceNode(ResultActions result, UUID userId) throws Exception {
        JsonNode json = objectMapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
        for (JsonNode entry : json) {
            if (entry.get("userId").asString().equals(userId.toString())) {
                return entry;
            }
        }
        throw new AssertionError("Balance entry not found for user: " + userId);
    }

    @Test
    void groupBalanceReflectsExpenseSplit() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        createTransaction(groupId, ownerId, "100", List.of(new ShareRequest(ownerId, new BigDecimal("50")), new ShareRequest(memberId, new BigDecimal("50"))));

        ResultActions result = getGroupBalance(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        assertThat(balanceNode(result, ownerId).get("netAmount").asDouble()).isEqualTo(50.00);
        assertThat(balanceNode(result, memberId).get("netAmount").asDouble()).isEqualTo(-50.00);
    }

    @Test
    void myBalanceShowsPairwiseDetail() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        createTransaction(groupId, ownerId, "100", List.of(new ShareRequest(ownerId, new BigDecimal("50")), new ShareRequest(memberId, new BigDecimal("50"))));

        getMyBalance(groupId, memberId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netAmount").value(-50.00))
                .andExpect(jsonPath("$.perCounterpart.length()").value(1))
                .andExpect(jsonPath("$.perCounterpart[0].counterpartId").value(ownerId.toString()))
                .andExpect(jsonPath("$.perCounterpart[0].netAmount").value(-50.00));
    }

    @Test
    void settlementReducesBalanceToZero() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        createTransaction(groupId, ownerId, "100", List.of(new ShareRequest(ownerId, new BigDecimal("50")), new ShareRequest(memberId, new BigDecimal("50"))));
        createSettlement(groupId, memberId, ownerId, "50");

        getMyBalance(groupId, memberId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netAmount").value(0.00))
                .andExpect(jsonPath("$.perCounterpart.length()").value(0));
    }

    @Test
    void balanceByNonMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getGroupBalance(groupId, strangerId).andExpect(status().isNotFound());
        getMyBalance(groupId, strangerId).andExpect(status().isNotFound());
    }

    private record CreateGroupRequest(String name) { }

    private record InviteMemberRequest(UUID userId) { }

    private record ShareRequest(UUID userId, BigDecimal percentage) { }

    private record CreateTransactionRequest(String description, BigDecimal amount, List<ShareRequest> shares) { }

    private record CreateSettlementRequest(UUID payeeId, BigDecimal amount) { }
}
