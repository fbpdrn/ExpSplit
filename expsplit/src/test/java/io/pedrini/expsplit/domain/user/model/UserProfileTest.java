package io.pedrini.expsplit.domain.user.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserProfileTest {

    private static final UserProfileId ID = new UserProfileId(java.util.UUID.randomUUID());
    private static final UserProfileEmail EMAIL = new UserProfileEmail("test@example.com");

    @Test
    void createNoName() {
        UserProfile userProfile = UserProfile.create(ID, EMAIL);

        assertThat(userProfile.id()).isEqualTo(ID);
        assertThat(userProfile.email()).isEqualTo(EMAIL);
        assertThat(userProfile.firstName()).isNull();
        assertThat(userProfile.lastName()).isNull();
        assertThat(userProfile.createdAt()).isNotNull();
    }

    @Test
    void updateName() {
        UserProfile userProfile = UserProfile.create(ID, EMAIL);
        FirstName firstName = new FirstName("Mario");
        LastName lastName = new LastName("Rossi");

        userProfile.updateName(firstName, lastName);

        assertThat(userProfile.firstName()).isEqualTo(firstName);
        assertThat(userProfile.lastName()).isEqualTo(lastName);
    }
}
