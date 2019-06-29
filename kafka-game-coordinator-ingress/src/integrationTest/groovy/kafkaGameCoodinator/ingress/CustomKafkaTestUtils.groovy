package kafkaGameCoodinator.ingress

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

class CustomKafkaTestUtils {

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
}
