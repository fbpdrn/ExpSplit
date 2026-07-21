package io.pedrini.auth;

import io.pedrini.auth.domain.user.model.UserAuth;
import io.pedrini.auth.domain.user.model.UserAuthEmail;
import io.pedrini.auth.domain.user.model.UserAuthStatus;
import io.pedrini.auth.domain.user.port.out.UserAuthRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import tools.jackson.databind.ObjectMapper;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAuthRepository userAuthRepository;

    private ResultActions register(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestBody(email, password))));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RequestBody(email, password))));
    }

    @Test
    void registerSave() throws Exception {
        String email = "email@example.com";
        String rawPassword = "password";

        // Si crea l'utente
        register(email, rawPassword).andExpect(status().isOk());

        // Si cerca l'utente
        UserAuth userAuth = userAuthRepository.findByEmail(new UserAuthEmail(email)).orElseThrow();

        // Si veificano i campi iniziali
        assertThat(userAuth.email().email()).isEqualTo(email);
        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.ACTIVE);
        assertThat(userAuth.failedAttempts()).isEqualTo(0);
        assertThat(userAuth.password().hash()).isNotEqualTo(rawPassword);
        assertThat(userAuth.password().hash()).isNotBlank();
    }

    @Test
    void registerDuplicate() throws Exception {
        String email = "email@example.com";

        // Si crea l'utente
        register(email, "password").andExpect(status().isOk());
        // Si prova a creare un altro utente con la stessa email
        register(email, "password").andExpect(status().isConflict());
    }

    @Test
    void loginJwtParse() throws Exception {
        String email = "email@example.com";

        // Si crea l'utente
        register(email, "password").andExpect(status().isOk());

        // Si cerca l'utente
        UserAuth userAuth = userAuthRepository.findByEmail(new UserAuthEmail(email)).orElseThrow();

        // Si verifica il token jwt
        MvcResult result = login(email, "password").andExpect(status().isOk()).andReturn();
        String token = objectMapper.readTree(result.getResponse().getContentAsByteArray()).get("token").asString();
        String[] parts = token.split("\\.");
        assertThat(parts).hasSize(3);
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
        assertThat(payloadJson).contains(userAuth.id().id().toString());
    }

    @Test
    void loginFailedAttempt() throws Exception {
        String email = "email@example.com";

        // Si crea l'utente
        register(email, "password");

        // Si tenta il login con password errata
        login(email, "wrong-password").andExpect(status().isUnauthorized());

        // Si verifica l'incremento dei tentativi falliti
        UserAuth userAuth = userAuthRepository.findByEmail(new UserAuthEmail(email)).orElseThrow();
        assertThat(userAuth.failedAttempts()).isEqualTo(1);
        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.ACTIVE);
    }

    @Test
    void loginFailedLocked() throws Exception {
        String email = "email@example.com";

        // Si crea l'utente
        register(email, "password");

        // Si tenta il login con un password errata fino al blocco dell'account
        for (int i = 0; i < UserAuth.MAX_FAILED_ATTEMPTS; i++) {
            login(email, "wrong-password").andExpect(status().isUnauthorized());
        }

        // Si verifica il login fallito con password corretta
        login(email, "password").andExpect(status().isForbidden());

        // Si verifica lo stato dell'account
        UserAuth userAuth = userAuthRepository.findByEmail(new UserAuthEmail(email)).orElseThrow();
        assertThat(userAuth.status()).isEqualTo(UserAuthStatus.LOCKED);
    }

    private record RequestBody(String email, String password) { }
}
