package kafkaGameCoordinator.common.kafka

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.producer.ProducerConfig
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

public class KafkaTestConsumer<K, V> {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(KafkaTestConsumer.class)

    private Class keySerializerClass
    private Class keyDeserializerClass
    private Class valueSerializerClass
    private Class valueDeserializerClass

    public KafkaTestConsumer(Class keySerializerClass, Class keyDeserializerClass,
                             Class valueSerializerClass, Class valueDeserializerClass) {
        this.keySerializerClass = keySerializerClass
        this.keyDeserializerClass = keyDeserializerClass
        this.valueSerializerClass = valueSerializerClass
        this.valueDeserializerClass = valueDeserializerClass
    }

    public Map<String, Object> producerConfigs(String brokerAddress) {
        Map<String, Object> props = new HashMap<>()
        // list of host:port pairs used for establishing the initial connections to the Kakfa cluster
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokerAddress)
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                  this.keySerializerClass)
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                  this.valueSerializerClass)

        return props
    }

    public ProducerFactory<K, V> producerFactory(String brokerAddress) {
        return new DefaultKafkaProducerFactory<>(producerConfigs(brokerAddress))
    }

    public KafkaTemplate<K, V> kafkaTemplate(String brokerAddress) {
        return new KafkaTemplate<>(producerFactory(brokerAddress))
    }

    public void setupKafkaConsumer(EmbeddedKafkaRule embeddedKafka, String topic, BlockingQueue<ConsumerRecord<K, V>> records) {
        // set up the Kafka consumer properties
        Map<String, Object> consumerProperties =
                KafkaTestUtils.consumerProps("int-test-consumer", "false",
                        embeddedKafka.getEmbeddedKafka())
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, this.keyDeserializerClass)
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, this.valueDeserializerClass)

        // create a Kafka consumer factory
        DefaultKafkaConsumerFactory<K, V> consumerFactory =
                new DefaultKafkaConsumerFactory<K, V>(consumerProperties)

        // set the topic that needs to be consumed
        ContainerProperties containerProperties =
                new ContainerProperties(topic)

        KafkaMessageListenerContainer<K, V> container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties)

        // setup a Kafka message listener
        container
                .setupMessageListener(new MessageListener<K, V>() {
                    @Override
                    void onMessage(ConsumerRecord<K, V> record) {
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
