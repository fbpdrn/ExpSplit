package io.pedrini.expsplit.infrastructure.balance;

import io.pedrini.expsplit.application.balance.BalanceService;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BalanceUseCaseConfig {

    @Bean
    public BalanceService balanceService(GroupRepository groupRepository, TransactionRepository transactionRepository,
                                          SettlementRepository settlementRepository) {
        return new BalanceService(groupRepository, transactionRepository, settlementRepository);
    }
}
