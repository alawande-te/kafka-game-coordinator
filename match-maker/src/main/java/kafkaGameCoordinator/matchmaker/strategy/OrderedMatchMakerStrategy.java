package kafkaGameCoordinator.matchmaker.strategy;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.IntegerDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class OrderedMatchMakerStrategy implements MatchMakingStrategy {

    private final String brokers;
    private final String group;
    private final String topic;

    public OrderedMatchMakerStrategy(String brokers, String group, String topic) {
        this.brokers = brokers;
        this.group = group;
        this.topic = topic;
    }

    public AbstractMessageListenerContainer makeContainer() {
        Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, group);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        consumerProperties.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "10");
        consumerProperties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "60000");
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, IntegerDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        // create a Kafka consumer factory
        ConsumerFactory<Integer, String> consumerFactory =
                new DefaultKafkaConsumerFactory<>(consumerProperties);

        ConcurrentKafkaListenerContainerFactory<Integer, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setBatchListener(true);

        AbstractMessageListenerContainer<Integer, String> container = factory.createContainer(topic);

//        // setup a Kafka message listener
//        container
//        .setupMessageListener(new MessageListener<Integer, List<String>>() {
//            @Override
//            public void onMessage(ConsumerRecord<Integer, List<String>> data) {
//                log.info(data.value().toString());
//            }
//        });
//
//        // start the container and underlying message listener
//        container.start();

        return container;
    }
}
