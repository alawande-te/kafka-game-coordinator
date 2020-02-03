package kafkaGameCoordinator.matchmaker

import kafkaGameCoordinator.matchmaker.kafka.KafkaContainerFacade
import kafkaGameCoordinator.matchmaker.strategy.MatchMakingStrategy
import kafkaGameCoordinator.models.EnrichedMessage
import kafkaGameCoordinator.serialization.EnrichedMessageDeserializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.kafka.listener.BatchMessageListener

import java.util.concurrent.CountDownLatch

@TestConfiguration
class ContainerWithCountdownConfiguration {

    public static CountDownLatch countDownLatch

    @Bean
    @Primary
    MatchMakingStrategy mockMatchMakingStrategy() {
        return new MatchMakingStrategy() {
            KafkaContainerFacade getStrategyFacade() {
                return KafkaContainerFacade.builder()
                        .isBatchProcessing(true)
                        .keyDeserializer(IntegerDeserializer.class)
                        .valueDeserializer(EnrichedMessageDeserializer.class)
                        .messageListener(getMessageListener())
                        .build()
            }

            BatchMessageListener getMessageListener() {
                return new BatchMessageListener<Integer, EnrichedMessage>() {
                    @Override
                    void onMessage(List<ConsumerRecord<Integer, EnrichedMessage>> data) {
                        for (ConsumerRecord<Integer, EnrichedMessage> record : data) {
                            System.out.println(record.value())
                            countDownLatch.countDown()
                        }
                    }
                }
            }
        }
    }
}
