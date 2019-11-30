package kafkaGameCoordinator.enricher

import com.google.common.collect.ImmutableSet
import kafkaGameCoordinator.common.kafka.KafkaTestConsumer
import kafkaGameCoordinator.models.EnrichedMessage
import kafkaGameCoordinator.models.UserStatus
import kafkaGameCoordinator.enricher.repo.UserRepo
import kafkaGameCoordinator.enricher.service.EnricherService
import kafkaGameCoordinator.models.IngressMessage
import kafkaGameCoordinator.serialization.IngressMessageDeserializer
import kafkaGameCoordinator.serialization.IngressMessageSerializer
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.rule.EmbeddedKafkaRule
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.util.ReflectionTestUtils
import spock.lang.Specification

import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue

@SpringBootTest(classes = EnricherApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
@DirtiesContext
//@Import(AuthThrottlerTestConfiguration.class)
class EnricherServiceTests extends Specification {

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    UserRepo userRepo

    @Autowired
    EnricherService enricherService

    EmbeddedKafkaRule embeddedKafka
    KafkaTestConsumer<String, EnrichedMessage> kafkaTestConsumer
    private KafkaTemplate<String, EnrichedMessage> kafkaTemplate
    private BlockingQueue<ConsumerRecord<String, EnrichedMessage>> records

    def setup() {
        jdbcTemplate.execute("DELETE FROM user_status")
        jdbcTemplate.execute("DELETE FROM user")

        kafkaTestConsumer = new KafkaTestConsumer<>(StringSerializer.class, StringDeserializer.class,
                                                    IngressMessageSerializer.class, IngressMessageDeserializer.class)
        embeddedKafka = new EmbeddedKafkaRule(1, true, 'ingress')
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
    }

    static IngressMessage ingressMessage(String authToken) {
        IngressMessage ingressMessage = new IngressMessage()
        ingressMessage.setAuthToken(authToken)
        return ingressMessage
    }
}
