package io.pedrini.expsplit.domain.group.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GroupNameTest {

    @Test
    void trimWhiteSpaces() {
        GroupName name = new GroupName("  Group A  ");

        assertThat(name.value()).isEqualTo("Group A");
    }

    @Test
    void blankException() {
        assertThatThrownBy(() -> new GroupName(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new GroupName("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullException() {
        assertThatThrownBy(() -> new GroupName(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void tooLongException() {
        String tooLong = "a".repeat(101);

        assertThatThrownBy(() -> new GroupName(tooLong))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
