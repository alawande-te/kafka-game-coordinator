package kafkaGameCoodinator.ingress

import kafkaGameCoordinator.ingress.Application
import kafkaGameCoordinator.ingress.controller.IngressController
import kafkaGameCoordinator.ingress.throttler.AuthThrottler
import org.apache.kafka.clients.consumer.ConsumerRecord
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

@SpringBootTest(classes = Application.class, properties = "spring.main.allow-bean-definition-overriding=true")
@DirtiesContext
@Import(AuthThrottlerTestConfiguration.class)
class IngressIntTest extends Specification {

    @Autowired
    private IngressController ingressController

    private static KafkaTemplate<String, String> kafkaTemplate
    boolean shouldAllow = true

    public EmbeddedKafkaRule embeddedKafka

    private BlockingQueue<ConsumerRecord<String, String>> records

    def cleanSpec() {
        embeddedKafka.after()
    }

    def setup() {
        embeddedKafka = new EmbeddedKafkaRule(1, true, 'ingress')
        embeddedKafka.before()
        kafkaTemplate = CustomKafkaTestUtils.kafkaTemplate(embeddedKafka.getEmbeddedKafka().brokerAddresses[0].toString())
        ReflectionTestUtils.setField(ingressController, "kafkaTemplate", kafkaTemplate)
        def stub = Stub(AuthThrottler)
        stub.processOne() >> { return shouldAllow }
        AuthThrottlerTestConfiguration.setMockAuthThrottler(stub)

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>()

        CustomKafkaTestUtils.setupKafkaConsumer(embeddedKafka, records)
    }

    def 'should receive kafka message when http message is received by ingress and is not throttled'() {
        when:
        String greeting = "hello"
        ingressController.ingressEntrypoint("auth")

        then:
        // check that the message was received
        ConsumerRecord<String, String> received =
                records.poll(10, TimeUnit.SECONDS)
        // Hamcrest Matchers to check the value
        MatcherAssert.assertThat(received, KafkaMatchers.hasValue(greeting))
        // AssertJ Condition to check the key
        MatcherAssert.assertThat(received, KafkaMatchers.hasKey(greeting))
    }

    def 'should not recive kafka message when http message is received by ingress and is throttled'() {
        when:
        String greeting = "hello"
        ingressController.ingressEntrypoint("auth")

        then:
        // check that the message was received
        ConsumerRecord<String, String> received =
                records.poll(10, TimeUnit.SECONDS)
        // Hamcrest Matchers to check the value
        MatcherAssert.assertThat(received, KafkaMatchers.hasValue(greeting))
        // AssertJ Condition to check the key
        MatcherAssert.assertThat(received, KafkaMatchers.hasKey(greeting))

        when:
        shouldAllow = false
        ingressController.ingressEntrypoint("auth")

        then:
        records.isEmpty()
    }
}
