package kafkaGameCoordinator.matchmaker

import kafkaGameCoordinator.common.kafka.KafkaTestConsumer
import kafkaGameCoordinator.matchmaker.strategy.MatchMakingStrategy
import kafkaGameCoordinator.matchmaker.strategy.OrderedMatchMakerStrategy
import kafkaGameCoordinator.models.EnrichedMessage
import kafkaGameCoordinator.serialization.EnrichedMessageDeserializer
import kafkaGameCoordinator.serialization.EnrichedMessageSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.IntegerDeserializer
import org.apache.kafka.common.serialization.IntegerSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.listener.AbstractMessageListenerContainer
import org.springframework.kafka.listener.BatchMessageListener
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.ContainerTestUtils
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

import java.util.concurrent.CountDownLatch

@SpringBootTest(classes = MatchMakerApplication.class)
@DirtiesContext
@EmbeddedKafka(topics = 'test')
class StrategyContainerMakerTests extends Specification {

    @Autowired
    EmbeddedKafkaBroker embeddedKafkaBroker

    MatchMakingStrategy matchMakingStrategy
    AbstractMessageListenerContainer container
    KafkaTemplate<Integer, EnrichedMessage> kafkaTemplate

    def setup() {

        matchMakingStrategy = new OrderedMatchMakerStrategy(embeddedKafkaBroker.getBrokersAsString(),
                'test-group', 'test')

        container = matchMakingStrategy.makeContainer()

        KafkaTestConsumer<Integer, EnrichedMessage> kafkaTestConsumer = new KafkaTestConsumer(IntegerSerializer.class, IntegerDeserializer.class,
                                                  EnrichedMessageSerializer.class, EnrichedMessageDeserializer.class)

        kafkaTemplate = kafkaTestConsumer.kafkaTemplate(embeddedKafkaBroker.getBrokersAsString())
    }

    def 'Should create the container based on the selected strategy'() {
        given:
        CountDownLatch countDownLatch = new CountDownLatch(2)

        when:
        container.setupMessageListener(new BatchMessageListener<Integer, EnrichedMessage>() {
            @Override
            void onMessage(List<ConsumerRecord<Integer, EnrichedMessage>> data) {
                for (ConsumerRecord<Integer, EnrichedMessage> record : data) {
                    System.out.println(record.value())
                    countDownLatch.countDown()
                }
            }
        })
        container.start()

        // wait until the container has the required number of assigned partitions
        ContainerTestUtils.waitForAssignment(container,
                embeddedKafkaBroker.getPartitionsPerTopic())

        kafkaTemplate.send("test", 1, [userId: 1, rank: 10] as EnrichedMessage)
        kafkaTemplate.send("test", 2, [userId: 2, rank: 100] as EnrichedMessage)
        kafkaTemplate.flush()

        then:
        countDownLatch.await()

        countDownLatch.getCount() == 0
    }
}
