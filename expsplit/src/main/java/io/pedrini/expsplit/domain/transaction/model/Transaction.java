package io.pedrini.expsplit.domain.transaction.model;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.exception.TransactionPermissionException;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Transaction {

    private final TransactionId id;
    private final GroupId groupId;
    private final UserProfileId paidBy;
    private TransactionDescription description;
    private Amount amount;
    private Category category;
    private List<TransactionShare> shares;
    private final Instant createdAt;

    public Transaction(TransactionId id, GroupId groupId, UserProfileId paidBy, TransactionDescription description,
                        Amount amount, Category category, List<TransactionShare> shares, Instant createdAt) {
        this.id = id;
        this.groupId = groupId;
        this.paidBy = paidBy;
        this.description = description;
        this.amount = amount;
        this.category = category;
        this.shares = new ArrayList<>(shares);
        this.createdAt = createdAt;
    }

    public static Transaction create(TransactionId id, GroupId groupId, UserProfileId paidBy,
                                      TransactionDescription description, Amount amount, Category category,
                                      List<TransactionShare> shares) {
        return new Transaction(id, groupId, paidBy, description, amount, orDefault(category), normalizeShares(shares), Instant.now());
    }

    public void update(TransactionDescription description, Amount amount, Category category, List<TransactionShare> shares) {
        this.description = description;
        this.amount = amount;
        this.category = orDefault(category);
        this.shares = new ArrayList<>(normalizeShares(shares));
    }

    private static Category orDefault(Category category) {
        return category != null ? category : Category.OTHER;
    }

    public void requireModifiable(UserProfileId requesterId, boolean requesterIsGroupOwner) {
        if (!paidBy.equals(requesterId) && !requesterIsGroupOwner) {
            throw new TransactionPermissionException(id, requesterId);
        }
    }

    private static final BigDecimal PERC_MAX = new BigDecimal("100");
    private static final BigDecimal PERC_TOL_PER_SHARE = new BigDecimal("0.01");

    /**
     * Normalizza la ripartizione nel caso di somma non perfetta al 100% per via dell'approssimazione
     * @param shares Lista delle ripartizioni
     * @return Lista delle ripartizioni normalizzate
     */
    private static List<TransactionShare> normalizeShares(List<TransactionShare> shares) {
        if (shares == null || shares.isEmpty()) {
            throw new IllegalArgumentException("Transaction must have at least one share");
        }

        Set<UserProfileId> seen = new HashSet<>();
        for (TransactionShare share : shares) {
            if (!seen.add(share.userId())) {
                throw new IllegalArgumentException("Duplicate share for user " + share.userId().id());
            }
        }

        BigDecimal total = shares.stream()
                .map(share -> share.percentage().value())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal gap = PERC_MAX.subtract(total);
        BigDecimal tolerance = PERC_TOL_PER_SHARE.multiply(BigDecimal.valueOf(shares.size()));

        if (gap.abs().compareTo(tolerance) > 0) {
            throw new IllegalArgumentException("Share percentages must sum to 100 (+/- " + tolerance + "), got " + total);
        }

        if (gap.compareTo(BigDecimal.ZERO) == 0) {
            return List.copyOf(shares);
        }

        List<TransactionShare> corrected = new ArrayList<>(shares);
        int lastIndex = corrected.size() - 1;
        TransactionShare last = corrected.get(lastIndex);
        corrected.set(lastIndex, new TransactionShare(last.userId(), new SharePercentage(last.percentage().value().add(gap))));
        return corrected;
    }

    public TransactionId id() { return id; }
    public GroupId groupId() { return groupId; }
    public UserProfileId paidBy() { return paidBy; }
    public TransactionDescription description() { return description; }
    public Amount amount() { return amount; }
    public Category category() { return category; }
    public List<TransactionShare> shares() { return List.copyOf(shares); }
    public Instant createdAt() { return createdAt; }
}
