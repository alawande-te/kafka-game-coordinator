package kafkaGameCoordinator.enricher

import com.google.common.collect.ImmutableSet
import kafkaGameCoordinator.enricher.models.UserStatus
import kafkaGameCoordinator.enricher.repo.UserRepo
import kafkaGameCoordinator.enricher.service.EnricherService
import kafkaGameCoordinator.models.IngressMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.annotation.DirtiesContext
import spock.lang.Specification

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

    def setup() {
        jdbcTemplate.execute("DELETE FROM user_status")
        jdbcTemplate.execute("DELETE FROM user")
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
