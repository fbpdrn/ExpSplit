package io.pedrini.expsplit.adapters.out.persistence.transaction;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.SharePercentage;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionDescription;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

class TransactionMapper {

    static Transaction toDomain(TransactionEntity entity) {
        List<TransactionShare> shares = entity.getShares().stream()
                .map(s -> new TransactionShare(
                        new UserProfileId(s.getUserId()),
                        new SharePercentage(s.getPercentage())
                ))
                .toList();

        return new Transaction(
                new TransactionId(entity.getId()),
                new GroupId(entity.getGroupId()),
                new UserProfileId(entity.getPaidBy()),
                new TransactionDescription(entity.getDescription()),
                new Amount(entity.getAmount()),
                shares,
                entity.getCreatedAt()
        );
    }

    static TransactionEntity toEntity(Transaction transaction) {
        List<TransactionShareEmbeddable> shares = transaction.shares().stream()
                .map(s -> new TransactionShareEmbeddable(s.userId().id(), s.percentage().value()))
                .toList();

        return new TransactionEntity(
                transaction.id().id(),
                transaction.groupId().id(),
                transaction.paidBy().id(),
                transaction.description().value(),
                transaction.amount().value(),
                shares,
                transaction.createdAt()
        );
    }
}
