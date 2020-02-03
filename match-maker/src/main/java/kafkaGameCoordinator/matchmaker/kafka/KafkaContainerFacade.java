package kafkaGameCoordinator.matchmaker.kafka;

import lombok.Builder;
import lombok.Value;
import org.springframework.kafka.listener.GenericMessageListener;

@Value
@Builder
public class KafkaContainerFacade {
    private final boolean isBatchProcessing;
    private final Class keyDeserializer;
    private final Class valueDeserializer;
    private final GenericMessageListener messageListener;
}
