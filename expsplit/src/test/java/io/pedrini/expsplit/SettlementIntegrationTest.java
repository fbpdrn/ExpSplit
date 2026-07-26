package io.pedrini.expsplit;

import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository;
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
class SettlementIntegrationTest {

    private static final String GROUP = "Group";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private SettlementRepository settlementRepository;

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

    private ResultActions createSettlement(UUID groupId, UUID requesterId, UUID payeeId, String amount) throws Exception {
        return createSettlement(groupId, requesterId, payeeId, amount, null);
    }

    private ResultActions createSettlement(UUID groupId, UUID requesterId, UUID payeeId, String amount, Category category) throws Exception {
        CreateSettlementRequest request = new CreateSettlementRequest(payeeId, new BigDecimal(amount), category);

        return mockMvc.perform(post("/groups/" + groupId + "/settlements")
                .with(authenticatedAs(requesterId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getSettlement(UUID groupId, UUID settlementId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/settlements/" + settlementId).with(authenticatedAs(requesterId)));
    }

    private ResultActions listSettlements(UUID groupId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/settlements").with(authenticatedAs(requesterId)));
    }

    private ResultActions deleteSettlement(UUID groupId, UUID settlementId, UUID requesterId) throws Exception {
        return mockMvc.perform(delete("/groups/" + groupId + "/settlements/" + settlementId).with(authenticatedAs(requesterId)));
    }

    private UUID settlementId(ResultActions result) throws Exception {
        JsonNode json = objectMapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
        return UUID.fromString(json.get("id").asString());
    }

    @Test
    void createSettlement() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        createSettlement(groupId, memberId, ownerId, "20")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payer.id").value(memberId.toString()))
                .andExpect(jsonPath("$.payee.id").value(ownerId.toString()))
                .andExpect(jsonPath("$.amount").value(20.00))
                .andExpect(jsonPath("$.category").value("OTHER"));
    }

    @Test
    void createSettlementWithExplicitCategory() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        createSettlement(groupId, memberId, ownerId, "20", Category.FOOD)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("FOOD"));
    }

    @Test
    void createSettlementSelf() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        createSettlement(groupId, ownerId, ownerId, "20").andExpect(status().isBadRequest());
    }

    @Test
    void createSettlementMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        createSettlement(groupId, strangerId, ownerId, "20").andExpect(status().isNotFound());
    }

    @Test
    void createSettlementPayeeMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        createSettlement(groupId, ownerId, strangerId, "20").andExpect(status().isNotFound());
    }

    @Test
    void getSettlement() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);
        UUID settlementId = settlementId(createSettlement(groupId, memberId, ownerId, "20"));

        getSettlement(groupId, settlementId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(20.00));
    }

    @Test
    void getSettlementNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getSettlement(groupId, UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    @Test
    void listSettlements() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);
        createSettlement(groupId, memberId, ownerId, "10").andExpect(status().isOk());
        createSettlement(groupId, memberId, ownerId, "15").andExpect(status().isOk());

        listSettlements(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void deleteSettlementByPayer() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);
        UUID settlementId = settlementId(createSettlement(groupId, memberId, ownerId, "20"));

        deleteSettlement(groupId, settlementId, memberId).andExpect(status().isNoContent());

        assertThat(settlementRepository.findById(new SettlementId(settlementId))).isEmpty();
    }

    @Test
    void deleteSettlementOwner() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);
        UUID settlementId = settlementId(createSettlement(groupId, memberId, ownerId, "20"));

        deleteSettlement(groupId, settlementId, ownerId).andExpect(status().isNoContent());
    }

    @Test
    void deleteSettlementForbidden() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, payerId);
        addAcceptedMember(groupId, ownerId, otherMemberId);
        UUID settlementId = settlementId(createSettlement(groupId, payerId, ownerId, "20"));

        deleteSettlement(groupId, settlementId, otherMemberId).andExpect(status().isForbidden());
    }

    @Test
    void deleteSettlementNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        deleteSettlement(groupId, UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    private record CreateGroupRequest(String name) { }

    private record InviteMemberRequest(UUID userId) { }

    private record CreateSettlementRequest(UUID payeeId, BigDecimal amount, Category category) { }
}
