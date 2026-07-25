package io.pedrini.expsplit.infrastructure.settlement;

import io.pedrini.expsplit.application.settlement.SettlementService;
import io.pedrini.expsplit.domain.group.port.out.GroupRepository;
import io.pedrini.expsplit.domain.settlement.port.out.SettlementRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SettlementUseCaseConfig {

    @Bean
    public SettlementService settlementService(SettlementRepository settlementRepository, GroupRepository groupRepository) {
        return new SettlementService(settlementRepository, groupRepository);
    }
}
