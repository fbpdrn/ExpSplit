package io.pedrini.expsplit;

import io.pedrini.expsplit.domain.transaction.model.Category;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StatisticsIntegrationTest {

    private static final String GROUP = "Group";

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

    private void createTransaction(UUID groupId, UUID requesterId, String amount, Category category) throws Exception {
        CreateTransactionRequest request = new CreateTransactionRequest("Expense", new BigDecimal(amount), category,
                List.of(new ShareRequest(requesterId, new BigDecimal("100"))));

        mockMvc.perform(post("/groups/" + groupId + "/transactions")
                        .with(authenticatedAs(requesterId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    private ResultActions getStatistics(UUID groupId, UUID requesterId) throws Exception {
        return mockMvc.perform(get("/groups/" + groupId + "/statistics").with(authenticatedAs(requesterId)));
    }

    @Test
    void statisticsAggregateByCategory() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        createTransaction(groupId, ownerId, "30", Category.FOOD);
        createTransaction(groupId, ownerId, "20", Category.FOOD);
        createTransaction(groupId, ownerId, "15", Category.TRANSPORT);

        getStatistics(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("FOOD"))
                .andExpect(jsonPath("$[0].totalAmount").value(50.00))
                .andExpect(jsonPath("$[0].transactionCount").value(2))
                .andExpect(jsonPath("$[1].category").value("TRANSPORT"))
                .andExpect(jsonPath("$[1].totalAmount").value(15.00))
                .andExpect(jsonPath("$[1].transactionCount").value(1));
    }

    @Test
    void uncategorizedTransactionsCountAsOther() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);
        createTransaction(groupId, ownerId, "40", null);

        getStatistics(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].category").value("OTHER"))
                .andExpect(jsonPath("$[0].totalAmount").value(40.00));
    }

    @Test
    void statisticsEmptyWhenNoTransactions() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getStatistics(groupId, ownerId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void statisticsByNonMemberNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        UUID strangerId = UUID.randomUUID();
        createProfile(ownerId);
        UUID groupId = createGroup(ownerId);

        getStatistics(groupId, strangerId).andExpect(status().isNotFound());
    }

    @Test
    void statisticsGroupNotFound() throws Exception {
        UUID ownerId = UUID.randomUUID();
        createProfile(ownerId);

        getStatistics(UUID.randomUUID(), ownerId).andExpect(status().isNotFound());
    }

    private record CreateGroupRequest(String name) { }

    private record ShareRequest(UUID userId, BigDecimal percentage) { }

    private record CreateTransactionRequest(String description, BigDecimal amount, Category category, List<ShareRequest> shares) { }
}
