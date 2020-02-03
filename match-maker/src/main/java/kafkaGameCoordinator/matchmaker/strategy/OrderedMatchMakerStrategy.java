package kafkaGameCoordinator.matchmaker.strategy;

import kafkaGameCoordinator.matchmaker.kafka.KafkaContainerFacade;
import kafkaGameCoordinator.serialization.EnrichedMessageDeserializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.springframework.kafka.listener.BatchMessageListener;
import org.springframework.kafka.listener.GenericMessageListener;

import java.util.List;

@Slf4j
public class OrderedMatchMakerStrategy<K, V> implements MatchMakingStrategy {

    public KafkaContainerFacade getStrategyFacade() {
        return KafkaContainerFacade.builder()
                .isBatchProcessing(true)
                .keyDeserializer(IntegerDeserializer.class)
                .valueDeserializer(EnrichedMessageDeserializer.class)
                .messageListener(getMessageListener())
                .build();
    }

    private GenericMessageListener<List<ConsumerRecord<K, V>>> getMessageListener() {
        return new BatchMessageListener<K, V>() {
            @Override
            public void onMessage(List<ConsumerRecord<K, V>> data) {
                for (ConsumerRecord<K, V> record : data) {
                    System.out.println(record.value());
                }
            }
        };
    }
}
