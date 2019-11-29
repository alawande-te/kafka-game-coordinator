package kafkaGameCoordinator.enricher

import kafkaGameCoordinator.models.IngressMessage
import kafkaGameCoordinator.serialization.IngressMessageDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils

@TestConfiguration
class KafkaTestConfiguration {

    public static EmbeddedKafkaRule embeddedKafka

    static {
        embeddedKafka =
                new EmbeddedKafkaRule(1, true, "ingress")
        embeddedKafka.before()
    }

    public static void before() {
        embeddedKafka.before()
    }

    public static void after() {
        embeddedKafka.after()
    }

    @Bean
    @Primary
    public ConsumerFactory<String, String> consumerFactory() {
        embeddedKafka.before()
        Map<String, Object> props = new HashMap<>()
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafka.getEmbeddedKafka().brokerAddresses[0].toString())
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IngressMessageDeserializer.class)
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "EnricherApp")
        // maximum records per poll
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "10")
        return new DefaultKafkaConsumerFactory<>(props)
    }

    static void setupKafkaConsumer() {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("int-test-consumer", "false",
                        embeddedKafka.getEmbeddedKafka())
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, IngressMessageDeserializer.class)

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, IngressMessage> consumerFactory =
                new DefaultKafkaConsumerFactory(consumerProperties)

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("ingress")

        KafkaMessageListenerContainer<String, IngressMessage> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties)

        // setup a Kafka message listener
        container
                .setupMessageListener(new MessageListener<String, IngressMessage>() {
                    @Override
                    void onMessage(ConsumerRecord<String, IngressMessage> data) {

                    }
                })

        // start the container and underlying message listener
        container.start()

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic())
    }
}
