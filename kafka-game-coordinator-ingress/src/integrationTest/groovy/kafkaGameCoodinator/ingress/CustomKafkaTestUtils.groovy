package kafkaGameCoodinator.ingress

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils

import java.util.concurrent.BlockingQueue

class CustomKafkaTestUtils {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(CustomKafkaTestUtils.class)

    static
    Map<String, Object> producerConfigs(String brokerAddress) {
        Map<String, Object> props = new HashMap<>()
        // list of host:port pairs used for establishing the initial connections to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class)

        return props
    }

    static ProducerFactory<String, String> producerFactory(String brokerAddress) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(brokerAddress))
    }

    static KafkaTemplate<String, String> kafkaTemplate(String brokerAddress) {
        return new KafkaTemplate<>(producerFactory(brokerAddress))
    }

    static void setupKafkaConsumer(EmbeddedKafkaRule embeddedKafka, BlockingQueue<ConsumerRecord<String, String>> records) {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("int-test-consumer", "false",
                        embeddedKafka.getEmbeddedKafka())
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class)

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<String, String> consumerFactory =
                new DefaultKafkaConsumerFactory<String, String>(consumerProperties)

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties("ingress")

        KafkaMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties)

        // setup a Kafka message listener
        container
                .setupMessageListener(new MessageListener<String, String>() {
                    @Override
                    void onMessage(ConsumerRecord<String, String> record) {
                        LOGGER.debug("test-listener received message='{}'",
                                record.toString())
                        records.add(record)
                    }
                })

        // start the container and underlying message listener
        container.start()

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic())
    }
}
