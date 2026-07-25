package io.pedrini.expsplit.domain.transaction.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

public interface GetTransactionUseCase {

    Transaction get(GroupId groupId, TransactionId transactionId, UserProfileId requesterId);
}
