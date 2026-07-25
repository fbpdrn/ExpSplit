package io.pedrini.expsplit.adapters.out.persistence.transaction;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.transaction.model.Transaction;
import io.pedrini.expsplit.domain.transaction.model.TransactionId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
class TransactionRepository implements io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository {

    private final TransactionJpaRepository jpaRepository;

    TransactionRepository(TransactionJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity saved = jpaRepository.save(TransactionMapper.toEntity(transaction));
        return TransactionMapper.toDomain(saved);
    }

    @Override
    public Optional<Transaction> findById(TransactionId id) {
        return jpaRepository.findById(id.id()).map(TransactionMapper::toDomain);
    }

    @Override
    public List<Transaction> findByGroupId(GroupId groupId) {
        return jpaRepository.findByGroupId(groupId.id()).stream().map(TransactionMapper::toDomain).toList();
    }

    @Override
    public void deleteById(TransactionId id) {
        jpaRepository.deleteById(id.id());
    }
}
