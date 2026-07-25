package io.pedrini.expsplit.application.balance;

import io.pedrini.expsplit.domain.balance.model.Balance;
import io.pedrini.expsplit.domain.balance.model.BalanceCalculator;
import io.pedrini.expsplit.domain.balance.model.BalanceComparison;
import io.pedrini.expsplit.domain.balance.model.UserBalanceDetail;
import io.pedrini.expsplit.domain.balance.port.in.GetGroupBalanceUseCase;
import io.pedrini.expsplit.domain.balance.port.in.GetUserBalanceUseCase;
import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.model.Membership;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public class BalanceService implements GetGroupBalanceUseCase, GetUserBalanceUseCase {

    private final GroupRepository groupRepository;
    private final TransactionRepository transactionRepository;
    private final SettlementRepository settlementRepository;

    public BalanceService(GroupRepository groupRepository, TransactionRepository transactionRepository,
                           SettlementRepository settlementRepository) {
        this.groupRepository = groupRepository;
        this.transactionRepository = transactionRepository;
        this.settlementRepository = settlementRepository;
    }

    @Override
    public List<Balance> getGroupBalance(GroupId groupId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);

        BalanceCalculator calculator = buildCalculator(groupId);
        return group.members().stream()
                .map(Membership::userId)
                .map(userId -> new Balance(userId, calculator.netBalance(userId)))
                .toList();
    }

    @Override
    public UserBalanceDetail getUserBalance(GroupId groupId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);

        BalanceCalculator calculator = buildCalculator(groupId);
        List<BalanceComparison> perCounterpart = group.members().stream()
                .map(Membership::userId)
                .filter(userId -> !userId.equals(requesterId))
                .map(counterpartId -> new BalanceComparison(counterpartId, calculator.netOwedTo(counterpartId, requesterId)))
                .filter(comparison -> comparison.netAmount().signum() != 0)
                .toList();

        return new UserBalanceDetail(requesterId, calculator.netBalance(requesterId), perCounterpart);
    }

    private BalanceCalculator buildCalculator(GroupId groupId) {
        return BalanceCalculator.of(transactionRepository.findByGroupId(groupId), settlementRepository.findByGroupId(groupId));
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
}
