package io.pedrini.auth.domain.user.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserAuthIdTest {

    @Test
    void nullException() {
        assertThatThrownBy(() -> new UserAuthId(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void uniqueIds() {
        List<UserAuthId> ids = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            ids.add(UserAuthId.generate());
        }

        List<UserAuthId> uniqueIds = ids.stream().distinct().toList();
        assertThat(uniqueIds.size()).isEqualTo(ids.size());
    }
}
