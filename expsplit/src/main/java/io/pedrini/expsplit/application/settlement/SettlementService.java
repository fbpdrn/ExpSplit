package io.pedrini.expsplit.application.settlement;

import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.settlement.exception.SettlementNotFoundException;
import io.pedrini.expsplit.domain.settlement.model.Settlement;
import io.pedrini.expsplit.domain.settlement.model.SettlementId;
import io.pedrini.expsplit.domain.settlement.port.in.CreateSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.DeleteSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.GetSettlementUseCase;
import io.pedrini.expsplit.domain.settlement.port.in.ListSettlementsUseCase;
import io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository;
import io.pedrini.expsplit.domain.transaction.model.Amount;
import io.pedrini.expsplit.domain.transaction.model.Category;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public class SettlementService implements CreateSettlementUseCase, GetSettlementUseCase, ListSettlementsUseCase, DeleteSettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final GroupRepository groupRepository;

    public SettlementService(SettlementRepository settlementRepository, GroupRepository groupRepository) {
        this.settlementRepository = settlementRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public Settlement create(GroupId groupId, UserProfileId payerId, UserProfileId payeeId, Amount amount, Category category) {
        Group group = loadGroup(groupId);
        requireMember(group, payerId);
        requireMember(group, payeeId);

        Settlement settlement = Settlement.create(SettlementId.generate(), groupId, payerId, payeeId, amount, category);
        return settlementRepository.save(settlement);
    }

    @Override
    public Settlement get(GroupId groupId, SettlementId settlementId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);
        return loadSettlement(groupId, settlementId);
    }

    @Override
    public List<Settlement> list(GroupId groupId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        requireMember(group, requesterId);
        return settlementRepository.findByGroupId(groupId);
    }

    @Override
    public void delete(GroupId groupId, SettlementId settlementId, UserProfileId requesterId) {
        Group group = loadGroup(groupId);
        Settlement settlement = loadSettlement(groupId, settlementId);
        settlement.requireDeletable(requesterId, group.isOwner(requesterId));
        settlementRepository.deleteById(settlementId);
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

    private Settlement loadSettlement(GroupId groupId, SettlementId settlementId) {
        return settlementRepository.findById(settlementId)
                .filter(settlement -> settlement.groupId().equals(groupId))
                .orElseThrow(() -> new SettlementNotFoundException(settlementId));
    }
}
