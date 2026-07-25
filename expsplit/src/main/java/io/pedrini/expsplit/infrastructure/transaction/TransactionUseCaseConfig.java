package io.pedrini.expsplit.infrastructure.transaction;

import io.pedrini.expsplit.application.transaction.TransactionService;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.transaction.port.out.TransactionRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TransactionUseCaseConfig {

    @Bean
    public TransactionService transactionService(TransactionRepository transactionRepository, GroupRepository groupRepository) {
        return new TransactionService(transactionRepository, groupRepository);
    }
}
