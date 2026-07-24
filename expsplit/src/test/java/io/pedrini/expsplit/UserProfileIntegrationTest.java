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
import tools.jackson.databind.ObjectMapper;

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
class UserProfileIntegrationTest {

    private static final String EMAIL = "test@example.com";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserProfileRepository userProfileRepository;

    private static RequestPostProcessor authenticatedAs(UUID userId) {
        return SecurityMockMvcRequestPostProcessors.jwt().jwt(builder -> builder.subject(userId.toString()).claim("email", UserProfileIntegrationTest.EMAIL));
    }

    private ResultActions createProfile(UUID userId) throws Exception {
        return mockMvc.perform(post("/profile").with(authenticatedAs(userId)));
    }

    private ResultActions getProfile(UUID userId) throws Exception {
        return mockMvc.perform(get("/profile").with(authenticatedAs(userId)));
    }

    private ResultActions updateProfile(UUID userId, String firstName, String lastName) throws Exception {
        return mockMvc.perform(patch("/profile")
                .with(authenticatedAs(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RequestBody(firstName, lastName))));
    }

    private ResultActions deleteProfile(UUID userId) throws Exception {
        return mockMvc.perform(delete("/profile").with(authenticatedAs(userId)));
    }

    @Test
    void createProfile() throws Exception {
        UUID userId = UUID.randomUUID();

        createProfile(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL))
                .andExpect(jsonPath("$.firstName").doesNotExist())
                .andExpect(jsonPath("$.lastName").doesNotExist());

        UserProfile userProfile = userProfileRepository.findById(new UserProfileId(userId)).orElseThrow();
        assertThat(userProfile.email()).isEqualTo(new UserProfileEmail(EMAIL));
        assertThat(userProfile.firstName()).isNull();
        assertThat(userProfile.lastName()).isNull();
    }

    @Test
    void createProfileConflict() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId).andExpect(status().isOk());
        createProfile(userId).andExpect(status().isConflict());
    }

    @Test
    void getProfileNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        getProfile(userId).andExpect(status().isNotFound());
    }

    @Test
    void getProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId).andExpect(status().isOk());

        getProfile(userId)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(EMAIL));
    }

    @Test
    void updateName() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId).andExpect(status().isOk());

        updateProfile(userId, "Mario", "Rossi")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Mario"))
                .andExpect(jsonPath("$.lastName").value("Rossi"));

        UserProfile userProfile = userProfileRepository.findById(new UserProfileId(userId)).orElseThrow();
        assertThat(userProfile.firstName().value()).isEqualTo("Mario");
        assertThat(userProfile.lastName().value()).isEqualTo("Rossi");
    }

    @Test
    void updateNameNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        updateProfile(userId, "Mario", "Rossi").andExpect(status().isNotFound());
    }

    @Test
    void updateNameBlank() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId).andExpect(status().isOk());

        updateProfile(userId, " ", "Rossi").andExpect(status().isBadRequest());
        updateProfile(userId, "Mario", " ").andExpect(status().isBadRequest());
        updateProfile(userId, " ", " ").andExpect(status().isBadRequest());
        updateProfile(userId, "", "").andExpect(status().isBadRequest());
    }

    @Test
    void deleteProfile() throws Exception {
        UUID userId = UUID.randomUUID();
        createProfile(userId).andExpect(status().isOk());

        deleteProfile(userId).andExpect(status().isNoContent());

        assertThat(userProfileRepository.findById(new UserProfileId(userId))).isEmpty();
        getProfile(userId).andExpect(status().isNotFound());
    }

    @Test
    void deleteProfileNotFound() throws Exception {
        UUID userId = UUID.randomUUID();
        deleteProfile(userId).andExpect(status().isNotFound());
    }

    private record RequestBody(String firstName, String lastName) { }
}
