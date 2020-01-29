package kafkaGameCoordinator.matchmaker.strategy;

import org.springframework.kafka.listener.AbstractMessageListenerContainer;

public interface MatchMakingStrategy {

    AbstractMessageListenerContainer makeContainer();
}
