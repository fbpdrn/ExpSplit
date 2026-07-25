package io.pedrini.expsplit.domain.transaction.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionDescription;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface CreateTransactionUseCase {

    Transaction create(GroupId groupId, UserProfileId requesterId, TransactionDescription description,
                        Amount amount, List<TransactionShare> shares);
}
