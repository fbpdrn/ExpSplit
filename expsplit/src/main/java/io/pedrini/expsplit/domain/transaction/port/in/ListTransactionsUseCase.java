package io.pedrini.expsplit.domain.transaction.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface ListTransactionsUseCase {

    List<Transaction> list(GroupId groupId, UserProfileId requesterId);
}
