package kafkaGameCoordinator.enricher

import com.google.common.collect.ImmutableSet
import kafkaGameCoordinator.common.kafka.KafkaTestConsumer
import kafkaGameCoordinator.enricher.repo.UserRepo
import kafkaGameCoordinator.enricher.service.EnricherService
import kafkaGameCoordinator.models.EnrichedMessage
import kafkaGameCoordinator.models.IngressMessage
import kafkaGameCoordinator.models.UserStatus
import kafkaGameCoordinator.serialization.EnrichedMessageDeserializer
import kafkaGameCoordinator.serialization.EnrichedMessageSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.LongDeserializer
import org.apache.kafka.common.serialization.LongSerializer
import org.hamcrest.MatcherAssert
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.hamcrest.KafkaMatchers
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.TimeUnit

import static org.hamcrest.core.IsCollectionContaining.hasItem

@SpringBootTest(classes = EnricherApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
@DirtiesContext
class EnricherServiceTests extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    UserRepo userRepo

    @Autowired
    EnricherService enricherService

    EmbeddedKafkaRule embeddedKafka
    KafkaTestConsumer<Long, EnrichedMessage> kafkaTestConsumer
    private KafkaTemplate<Long, EnrichedMessage> kafkaTemplate
    private BlockingQueue<ConsumerRecord<Long, EnrichedMessage>> records

    def setup() {
        jdbcTemplate.execute("DELETE FROM user_status")
        jdbcTemplate.execute("DELETE FROM user")

        kafkaTestConsumer = new KafkaTestConsumer<>(LongSerializer.class, LongDeserializer.class,
                                                    EnrichedMessageSerializer.class, EnrichedMessageDeserializer.class)
        embeddedKafka = new EmbeddedKafkaRule(1, true, 'enriched')
        embeddedKafka.before()
        kafkaTemplate = kafkaTestConsumer.kafkaTemplate(embeddedKafka.getEmbeddedKafka().brokerAddresses[0].toString())
        ReflectionTestUtils.setField(enricherService, "kafkaTemplate", kafkaTemplate)

        // create a thread safe queue to store the received message
        records = new LinkedBlockingQueue<>()

        kafkaTestConsumer.setupKafkaConsumer(embeddedKafka, "enriched", records)
    }

    def 'should update user status and timestamps when enriched'() {
        given: 'a couple of users with varying status'
        jdbcTemplate.execute("INSERT INTO user VALUES(1, 1, 'aa')")
        jdbcTemplate.execute("INSERT INTO user_status VALUES(1, 'IDLE', 10)")
        jdbcTemplate.execute("INSERT INTO user VALUES(2, 2, 'ab')")
        jdbcTemplate.execute("INSERT INTO user_status VALUES(2, 'FINDING', 12)")
        jdbcTemplate.execute("INSERT INTO user VALUES(3, 3, 'ac')")
        jdbcTemplate.execute("INSERT INTO user_status VALUES(3, 'FINDING', 13)")

        Collection<IngressMessage> messages = ImmutableSet.of(ingressMessage('aa'),
                                                              ingressMessage('ab'),
                                                              ingressMessage('ac'))

        when: 'Enrich users is called'
        enricherService.enricherUsers(messages)

        then: 'User statuses should be updated to FINDING'
        userRepo.getUsersByIdIn(ImmutableSet.of(1L, 2L, 3L))
                .values().every({ user -> user.getStatus() == UserStatus.FINDING })

        and: 'kafka messages were sent'
        List<ConsumerRecord<Long, EnrichedMessage>> received = new ArrayList<>();
        received.add(records.poll(10, TimeUnit.SECONDS))
        received.add(records.poll(10, TimeUnit.SECONDS))
        received.add(records.poll(10, TimeUnit.SECONDS))
        MatcherAssert.assertThat(received, hasItem(KafkaMatchers.hasKey(1L)))
        MatcherAssert.assertThat(received, hasItem(KafkaMatchers.hasKey(2L)))
        MatcherAssert.assertThat(received, hasItem(KafkaMatchers.hasKey(3L)))
    }

    static IngressMessage ingressMessage(String authToken) {
        IngressMessage ingressMessage = new IngressMessage()
        ingressMessage.setAuthToken(authToken)
        return ingressMessage
    }
}
