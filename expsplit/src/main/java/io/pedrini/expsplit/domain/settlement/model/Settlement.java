package io.pedrini.expsplit.domain.settlement.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.settlement.exception.SettlementPermissionException;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.time.Instant;

public class Settlement {

    private final SettlementId id;
    private final GroupId groupId;
    private final UserProfileId payerId;
    private final UserProfileId payeeId;
    private final Amount amount;
    private final Category category;
    private final Instant createdAt;

    public Settlement(SettlementId id, GroupId groupId, UserProfileId payerId, UserProfileId payeeId,
                       Amount amount, Category category, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.payerId = payerId;
        this.payeeId = payeeId;
        this.amount = amount;
        this.category = category;
        this.createdAt = createdAt;
    }

    public static Settlement create(SettlementId id, GroupId groupId, UserProfileId payerId, UserProfileId payeeId,
                                     Amount amount, Category category) {
        if (payerId.equals(payeeId)) {
            throw new IllegalArgumentException("A settlement cannot be paid to oneself");
        }
        Category resolvedCategory = category != null ? category : Category.OTHER;
        return new Settlement(id, groupId, payerId, payeeId, amount, resolvedCategory, Instant.now());
    }

    public void requireDeletable(UserProfileId requesterId, boolean requesterIsGroupOwner) {
        if (!payerId.equals(requesterId) && !requesterIsGroupOwner) {
            throw new SettlementPermissionException(id, requesterId);
        }
    }

    public SettlementId id() { return id; }
    public GroupId groupId() { return groupId; }
    public UserProfileId payerId() { return payerId; }
    public UserProfileId payeeId() { return payeeId; }
    public Amount amount() { return amount; }
    public Category category() { return category; }
    public Instant createdAt() { return createdAt; }
}
