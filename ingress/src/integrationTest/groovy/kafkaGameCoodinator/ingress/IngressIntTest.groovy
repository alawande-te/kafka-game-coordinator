package kafkaGameCoodinator.ingress

import kafkaGameCoodinator.utils.AuthThrottlerTestConfiguration
import kafkaGameCoordinator.common.kafka.KafkaTestConsumer
import kafkaGameCoordinator.ingress.IngressApplication
import kafkaGameCoordinator.ingress.controller.IngressController
import kafkaGameCoordinator.ingress.throttler.AuthThrottler
import kafkaGameCoordinator.models.IngressMessage
import kafkaGameCoordinator.serialization.IngressMessageDeserializer
import kafkaGameCoordinator.serialization.IngressMessageSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.hamcrest.MatcherAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.hamcrest.KafkaMatchers
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = IngressApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
@DirtiesContext
@Import(AuthThrottlerTestConfiguration.class)
class IngressIntTest extends Specification {

    @Autowired
    private IngressController ingressController

    private static KafkaTemplate<String, IngressMessage> kafkaTemplate
    boolean shouldAllow = true

    public EmbeddedKafkaRule embeddedKafka
    private KafkaTestConsumer<String, IngressMessage> kafkaTestConsumer
    private BlockingQueue<ConsumerRecord<String, IngressMessage>> records

    def cleanup() {
        embeddedKafka.after()
    }

    def setup() {
        kafkaTestConsumer = new KafkaTestConsumer<>(StringSerializer.class, StringDeserializer.class,
                                                    IngressMessageSerializer.class, IngressMessageDeserializer.class)
        embeddedKafka = new EmbeddedKafkaRule(1, true, 'ingress')
        embeddedKafka.before()
        kafkaTemplate = kafkaTestConsumer.kafkaTemplate(embeddedKafka.getEmbeddedKafka().brokerAddresses[0].toString())
        ReflectionTestUtils.setField(ingressController, "kafkaTemplate", kafkaTemplate)
        def stub = Stub(AuthThrottler)
        stub.processOne() >> { return shouldAllow }
        AuthThrottlerTestConfiguration.setMockAuthThrottler(stub)

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>()

        kafkaTestConsumer.setupKafkaConsumer(embeddedKafka, "ingress", records)
    }

    def 'should receive kafka message when http message is received by ingress and is not throttled'() {
        when:
        ingressController.ingressEntrypoint("auth")

        then:
        // check that the message was received
        ConsumerRecord<String, IngressMessage> received =
                records.poll(10, TimeUnit.SECONDS)
        received.value().authToken == "auth"
        MatcherAssert.assertThat(received, KafkaMatchers.hasKey("auth"))
    }

    def 'should not receive kafka message when http message is received by ingress and is throttled'() {
        when:
        ingressController.ingressEntrypoint("auth")

        then:
        // check that the message was received
        ConsumerRecord<String, IngressMessage> received =
                records.poll(10, TimeUnit.SECONDS)
        received.value().authToken == "auth"
        MatcherAssert.assertThat(received, KafkaMatchers.hasKey("auth"))

        when:
        shouldAllow = false
        ingressController.ingressEntrypoint("auth")

        then:
        records.isEmpty()
    }
}
