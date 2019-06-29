package kafkaGameCoodinator.ingress

import kafkaGameCoordinator.ingress.Application
import kafkaGameCoordinator.ingress.HelloController
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.hamcrest.MatcherAssert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.KafkaMessageListenerContainer
import org.springframework.kafka.listener.MessageListener
import org.springframework.kafka.test.hamcrest.KafkaMatchers
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = Application.class)
@DirtiesContext
class IngressIntTest extends Specification {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(IngressIntTest.class)

    @Autowired
    private HelloController helloController

    private static KafkaTemplate<String, String> kafkaTemplate

    public static EmbeddedKafkaRule embeddedKafka =
            new EmbeddedKafkaRule(1, true, 'ingress')

    private BlockingQueue<ConsumerRecord<String, String>> records

    def setupSpec() {
        embeddedKafka.before()
        kafkaTemplate = CustomKafkaTestUtils.kafkaTemplate(embeddedKafka.getEmbeddedKafka().brokerAddresses[0].toString())
    }

    def cleanupSpec() {
        embeddedKafka.after()
    }

    def setup() {
        ReflectionTestUtils.setField(helloController, "kafkaTemplate", kafkaTemplate)

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

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>()

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

    def 'should receive kafka message when http message is received by ingress and is not throttled'() {
        when:
        String greeting = "hello"
        helloController.index()

        then:
        // check that the message was received
        ConsumerRecord<String, String> received =
                records.poll(10, TimeUnit.SECONDS)
        // Hamcrest Matchers to check the value
        MatcherAssert.assertThat(received, KafkaMatchers.hasValue(greeting))
        // AssertJ Condition to check the key
        MatcherAssert.assertThat(received, KafkaMatchers.hasKey(greeting))
    }
}
