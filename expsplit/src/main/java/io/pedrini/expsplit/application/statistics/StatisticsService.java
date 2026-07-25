package io.pedrini.expsplit.application.statistics;

import io.pedrini.expsplit.domain.group.exception.GroupNotFoundException;
import io.pedrini.expsplit.domain.group.exception.NotGroupMemberException;
import io.pedrini.expsplit.domain.group.model.Group;
import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.statistics.model.CategoryStatistic;
import io.pedrini.expsplit.domain.statistics.model.StatisticsCalculator;
import io.pedrini.expsplit.domain.statistics.port.in.GetGroupStatisticsUseCase;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public class StatisticsService implements GetGroupStatisticsUseCase {

    private final GroupRepository groupRepository;
    private final TransactionRepository transactionRepository;

    public StatisticsService(GroupRepository groupRepository, TransactionRepository transactionRepository) {
        this.groupRepository = groupRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<CategoryStatistic> getStatistics(GroupId groupId, UserProfileId requesterId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new GroupNotFoundException(groupId));

        if (!group.isMember(requesterId)) {
            throw new NotGroupMemberException(groupId, requesterId);
        }

        return StatisticsCalculator.summarize(transactionRepository.findByGroupId(groupId));
    }
}
