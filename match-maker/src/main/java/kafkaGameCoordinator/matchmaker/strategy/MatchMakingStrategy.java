package kafkaGameCoordinator.matchmaker.strategy;

import kafkaGameCoordinator.matchmaker.kafka.KafkaContainerFacade;

public interface MatchMakingStrategy {

    KafkaContainerFacade getStrategyFacade();
}
