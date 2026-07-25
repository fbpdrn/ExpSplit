package io.pedrini.expsplit.infrastructure.statistics;

import io.pedrini.expsplit.application.statistics.StatisticsService;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StatisticsUseCaseConfig {

    @Bean
    public StatisticsService statisticsService(GroupRepository groupRepository, TransactionRepository transactionRepository) {
        return new StatisticsService(groupRepository, transactionRepository);
    }
}
