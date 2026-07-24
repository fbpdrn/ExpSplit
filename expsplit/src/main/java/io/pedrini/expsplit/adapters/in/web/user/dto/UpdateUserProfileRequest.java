package io.pedrini.expsplit.adapters.in.web.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateUserProfileRequest(@NotBlank String firstName, @NotBlank String lastName) {
}
