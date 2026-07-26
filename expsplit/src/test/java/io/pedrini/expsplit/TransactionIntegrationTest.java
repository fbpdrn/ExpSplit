package io.pedrini.expsplit;

import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TransactionIntegrationTest {

    private static final String GROUP = "Group";
    private static final String DESCRIPTION = "Description";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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

    private ResultActions createTransaction(UUID groupId, UUID requesterId, CreateTransactionRequest request) throws Exception {
        return mockMvc.perform(post("/groups/" + groupId + "/transactions")
                .with(authenticatedAs(requesterId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions getTransaction(UUID groupId, UUID transactionId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/transactions/" + transactionId).with(authenticatedAs(requesterId)));
    }

    private ResultActions listTransactions(UUID groupId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/transactions").with(authenticatedAs(requesterId)));
    }

    private ResultActions updateTransaction(UUID groupId, UUID transactionId, UUID requesterId, CreateTransactionRequest request) throws Exception {
        return mockMvc.perform(patch("/groups/" + groupId + "/transactions/" + transactionId)
                .with(authenticatedAs(requesterId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));
    }

    private ResultActions deleteTransaction(UUID groupId, UUID transactionId, UUID requesterId) throws Exception {
        return mockMvc.perform(delete("/groups/" + groupId + "/transactions/" + transactionId).with(authenticatedAs(requesterId)));
    }

    private UUID transactionId(ResultActions result) throws Exception {
        JsonNode json = objectMapper.readTree(result.andReturn().getResponse().getContentAsByteArray());
        return UUID.fromString(json.get("id").asString());
    }

    @Test
    void createTransactionSplit() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);

        CreateTransactionRequest request = new CreateTransactionRequest(DESCRIPTION, new BigDecimal("100"),
                List.of(new ShareRequest(ownerId, new BigDecimal("40")), new ShareRequest(memberId, new BigDecimal("60"))));

        createTransaction(groupId, ownerId, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paidBy.id").value(ownerId.toString()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.category").value("OTHER"))
                .andExpect(jsonPath("$.shares.length()").value(2));
    }

    @Test
    void createTransactionWithExplicitCategory() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        CreateTransactionRequest request = new CreateTransactionRequest(DESCRIPTION, new BigDecimal("50"), Category.TRANSPORT,
                List.of(new ShareRequest(ownerId, new BigDecimal("100"))));

        createTransaction(groupId, ownerId, request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("TRANSPORT"));
    }

    @Test
    void createTransactionMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID randomId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        CreateTransactionRequest request = new CreateTransactionRequest(DESCRIPTION, new BigDecimal("100"),
                List.of(new ShareRequest(ownerId, new BigDecimal("100"))));

        createTransaction(groupId, randomId, request).andExpect(status().isNotFound());
    }

    @Test
    void createTransactionShareMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID randomId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        CreateTransactionRequest request = new CreateTransactionRequest(DESCRIPTION, new BigDecimal("100"),
                List.of(new ShareRequest(ownerId, new BigDecimal("50")), new ShareRequest(randomId, new BigDecimal("50"))));

        createTransaction(groupId, ownerId, request).andExpect(status().isNotFound());
    }

    @Test
    void createTransactionSharesNotSum() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        CreateTransactionRequest request = new CreateTransactionRequest(DESCRIPTION, new BigDecimal("100"),
                List.of(new ShareRequest(ownerId, new BigDecimal("50"))));

        createTransaction(groupId, ownerId, request).andExpect(status().isBadRequest());
    }

    @Test
    void getTransaction() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        UUID transactionId = transactionId(createTransaction(groupId, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(ownerId, new BigDecimal("100"))))));

        getTransaction(groupId, transactionId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(DESCRIPTION));
    }

    @Test
    void getTransactionNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getTransaction(groupId, UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    @Test
    void getTransactionPermission() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupA = createGroup(ownerId);
        UUID groupB = createGroup(ownerId);
        UUID transactionId = transactionId(createTransaction(groupA, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(ownerId, new BigDecimal("100"))))));

        getTransaction(groupB, transactionId, ownerId).andExpect(status().isNotFound());
    }

    @Test
    void getTransactions() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        createTransaction(groupId, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(ownerId, new BigDecimal("100")))))
                .andExpect(status().isOk());
        createTransaction(groupId, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("50"), List.of(new ShareRequest(ownerId, new BigDecimal("100")))))
                .andExpect(status().isOk());

        listTransactions(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void updateTransaction() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        UUID transactionId = transactionId(createTransaction(groupId, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(ownerId, new BigDecimal("100"))))));

        updateTransaction(groupId, transactionId, ownerId, new CreateTransactionRequest(
                "Description", new BigDecimal("40"), List.of(new ShareRequest(ownerId, new BigDecimal("100")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.amount").value(40.00));
    }

    @Test
    void updateTransactionOwner() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, memberId);
        UUID transactionId = transactionId(createTransaction(groupId, memberId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(memberId, new BigDecimal("100"))))));

        updateTransaction(groupId, transactionId, ownerId, new CreateTransactionRequest(
                "Description", new BigDecimal("40"), List.of(new ShareRequest(ownerId, new BigDecimal("100")))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.amount").value(40.00));
    }

    @Test
    void updateTransactionPermission() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, payerId);
        addAcceptedMember(groupId, ownerId, otherMemberId);
        UUID transactionId = transactionId(createTransaction(groupId, payerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(payerId, new BigDecimal("100"))))));

        updateTransaction(groupId, transactionId, otherMemberId, new CreateTransactionRequest(
                "Pranzo", new BigDecimal("40"), List.of(new ShareRequest(payerId, new BigDecimal("100")))))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTransaction() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        UUID transactionId = transactionId(createTransaction(groupId, ownerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(ownerId, new BigDecimal("100"))))));

        deleteTransaction(groupId, transactionId, ownerId).andExpect(status().isNoContent());

        assertThat(transactionRepository.findById(new TransactionId(transactionId))).isEmpty();
    }

    @Test
    void deleteTransactionPermission() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID payerId = UUID.randomUUID();
        UUID otherMemberId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        addAcceptedMember(groupId, ownerId, payerId);
        addAcceptedMember(groupId, ownerId, otherMemberId);
        UUID transactionId = transactionId(createTransaction(groupId, payerId, new CreateTransactionRequest(
                DESCRIPTION, new BigDecimal("100"), List.of(new ShareRequest(payerId, new BigDecimal("100"))))));

        deleteTransaction(groupId, transactionId, otherMemberId).andExpect(status().isForbidden());
    }

    @Test
    void deleteTransactionNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        deleteTransaction(groupId, UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    private record CreateGroupRequest(String name) { }

    private record InviteMemberRequest(UUID userId) { }

    private record ShareRequest(UUID userId, BigDecimal percentage) { }

    private record CreateTransactionRequest(String description, BigDecimal amount, Category category, List<ShareRequest> shares) {

        CreateTransactionRequest(String description, BigDecimal amount, List<ShareRequest> shares) {
            this(description, amount, null, shares);
        }
    }
}
