package io.pedrini.expsplit.domain.statistics.port.in;

import io.pedrini.expsplit.domain.group.model.GroupId;
import io.pedrini.expsplit.domain.statistics.model.CategoryStatistic;
import io.pedrini.expsplit.domain.user.model.UserProfileId;

import java.util.List;

public interface GetGroupStatisticsUseCase {

    List<CategoryStatistic> getStatistics(GroupId groupId, UserProfileId requesterId);
}
