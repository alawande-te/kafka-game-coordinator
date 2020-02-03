package kafkaGameCoordinator.matchmaker.strategy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StrategyConfiguration {

    @Value("${match-maker.strategy}")
    private String configStrategy;

    @Bean
    public MatchMakingStrategy matchMakingStrategy() {
        switch (configStrategy) {
            case "OrderedMatchMakerStrategy":
                return new OrderedMatchMakerStrategy();
            default:
                throw new IllegalArgumentException("Unknown strategy");
        }
    }
}
