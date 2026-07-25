package io.pedrini.expsplit.adapters.in.web.group.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateGroupRequest(@NotBlank String name) {
}
