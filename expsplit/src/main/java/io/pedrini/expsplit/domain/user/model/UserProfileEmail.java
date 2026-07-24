package io.pedrini.expsplit.domain.user.model;

import java.util.regex.Pattern;

public record UserProfileEmail(String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public UserProfileEmail {
        if (email == null) {
            throw new IllegalArgumentException("Invalid email format: null");
        }

        email = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}
