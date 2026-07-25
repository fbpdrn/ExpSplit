package io.pedrini.expsplit.application.transaction;

import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.transaction.exception.TransactionNotFoundException;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionDescription;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import io.pedrini.expsplit.domain.transaction.model.TransactionShare;
import io.pedrini.expsplit.domain.transaction.port.in.CreateTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.DeleteTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.GetTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.ListTransactionsUseCase;
import io.pedrini.expsplit.domain.transaction.port.in.UpdateTransactionUseCase;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public class TransactionService implements CreateTransactionUseCase, GetTransactionUseCase, ListTransactionsUseCase,
        UpdateTransactionUseCase, DeleteTransactionUseCase {

    private final TransactionRepository transactionRepository;
    private final GroupRepository groupRepository;

    public TransactionService(TransactionRepository transactionRepository, GroupRepository groupRepository) {
        this.transactionRepository = transactionRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public Transaction create(GroupId groupId, UserProfileId requesterId, TransactionDescription description,
                               Amount amount, Category category, List<TransactionShare> shares) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);
        shares.forEach(share -> requireMember(group, share.userId()));

        Transaction transaction = Transaction.create(TransactionId.generate(), groupId, requesterId, description, amount, category, shares);
        return transactionRepository.save(transaction);
    }

    @Override
    public Transaction get(GroupId groupId, TransactionId transactionId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);
        return loadTransaction(groupId, transactionId);
    }

    @Override
    public List<Transaction> list(GroupId groupId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);
        return transactionRepository.findByGroupId(groupId);
    }

    @Override
    public Transaction update(GroupId groupId, TransactionId transactionId, UserProfileId requesterId,
                               TransactionDescription description, Amount amount, Category category, List<TransactionShare> shares) {
        Group group = loadGroup(groupId);
        Transaction transaction = loadTransaction(groupId, transactionId);
        transaction.requireModifiable(requesterId, group.isOwner(requesterId));
        shares.forEach(share -> requireMember(group, share.userId()));

        transaction.update(description, amount, category, shares);
        return transactionRepository.save(transaction);
    }

    @Override
    public void delete(GroupId groupId, TransactionId transactionId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        Transaction transaction = loadTransaction(groupId, transactionId);
        transaction.requireModifiable(requesterId, group.isOwner(requesterId));
        transactionRepository.deleteById(transactionId);
    }

    private Group loadGroup(GroupId groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));
    }

    private void requireMember(Group group, UserProfileId userId) {
        if (!group.isMember(userId)) {
            throw new NotGroupMemberException(group.id(), userId);
        }
    }

    private Transaction loadTransaction(GroupId groupId, TransactionId transactionId) {
        return transactionRepository.findById(transactionId)
                .filter(transaction -> transaction.groupId().equals(groupId))
                .orElseThrow(() -> new TransactionNotFoundException(transactionId));
    }
}
