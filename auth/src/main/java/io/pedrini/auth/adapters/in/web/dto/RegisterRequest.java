package io.pedrini.auth.adapters.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String email,
        @NotBlank @Size(min = 8, max = 128) String password
) {
}
