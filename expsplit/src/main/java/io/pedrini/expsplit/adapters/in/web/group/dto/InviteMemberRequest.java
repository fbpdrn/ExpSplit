package io.pedrini.expsplit.adapters.in.web.group.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record InviteMemberRequest(@NotNull UUID userId) {
}
