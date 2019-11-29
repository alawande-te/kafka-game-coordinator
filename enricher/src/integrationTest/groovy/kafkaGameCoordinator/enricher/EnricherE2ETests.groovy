package kafkaGameCoordinator.enricher

import kafkaGameCoordinator.models.IngressMessage
import kafkaGameCoordinator.serialization.IngressMessageSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Ignore
import spock.lang.Specification

@SpringBootTest(classes = EnricherApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
@DirtiesContext
@Import(KafkaTestConfiguration.class)
@Ignore
class EnricherE2ETests extends Specification {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(EnricherE2ETests.class)

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry

    private KafkaTemplate<String, IngressMessage> template

//    def before() {
//        KafkaTestConfiguration.embeddedKafka.before()
//    }

    def after() {
        KafkaTestConfiguration.embeddedKafka.after()
    }

    def setup() {
        Map<String, Object> senderProperties =
                KafkaTestUtils.senderProps(
                        KafkaTestConfiguration.embeddedKafka.getEmbeddedKafka().getBrokersAsString())
        senderProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class)
        senderProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, IngressMessageSerializer.class)

        ProducerFactory<String, IngressMessage> producerFactory =
                new DefaultKafkaProducerFactory(senderProperties)

        template = new KafkaTemplate<>(producerFactory)
        template.setDefaultTopic("ingress")

        KafkaTestConfiguration.setupKafkaConsumer()
    }

    def 'should populate embedded kafka address'() {
        when:
        int a = 0
        IngressMessage ingressMessage = new IngressMessage()
        ingressMessage.authToken = "test"
        ingressMessage.ts = System.currentTimeMillis()
        template.sendDefault("test", ingressMessage)

        then:
        System.out.println(KafkaTestConfiguration.embeddedKafka.getEmbeddedKafka().brokerAddresses[0])
    }
}
