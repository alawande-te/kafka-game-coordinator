package kafkaGameCoordinator.matchmaker.kafka;

import kafkaGameCoordinator.matchmaker.strategy.MatchMakingStrategy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class ContainerConfiguration {

    @Value("${kafka.consumer.brokers}")
    private String brokers;

    @Value("${kafka.consumer.topic}")
    private String topic;

    @Value("${kafka.consumer.groupId}")
    private String groupId;

    private final MatchMakingStrategy matchMakingStrategy;

    @Bean("listenerContainer")
    public AbstractMessageListenerContainer configureContainer() {
        KafkaContainerFacade kafkaContainerFacade = matchMakingStrategy.getStrategyFacade();

        Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
        consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaContainerFacade.getKeyDeserializer());
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaContainerFacade.getValueDeserializer());

        // create a Kafka consumer factory
        ConsumerFactory<Integer, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties);

        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(kafkaContainerFacade.isBatchProcessing());

        return factory.createContainer(topic);
    }

    @Bean("containerStarter")
    public AbstractMessageListenerContainer startContainer(@Qualifier("listenerContainer") AbstractMessageListenerContainer container) {
        container.setupMessageListener(matchMakingStrategy.getStrategyFacade().getMessageListener());

        container.start();

        return container;
    }
}
