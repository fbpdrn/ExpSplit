package io.pedrini.auth.domain.user.model;

import java.util.regex.Pattern;

public record UserAuthEmail(String email) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public UserAuthEmail {
        if (email == null) {
            throw new IllegalArgumentException("Invalid email format: null");
        }

        email = email.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }
    }
}
