package kafkaGameCoordinator.enricher

import kafkaGameCoordinator.enricher.models.User
import kafkaGameCoordinator.enricher.repo.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest(classes = EnricherApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
class EnricherIntTests extends Specification{

    @Autowired
    JdbcTemplate jdbcTemplate

    @Autowired
    UserRepo userRepo

    def setup() {
        jdbcTemplate.execute("DELETE FROM user_status")
        jdbcTemplate.execute("DELETE FROM user")
    }

    def 'should retrieve data from mysql'() {
        given:
        jdbcTemplate.execute("INSERT INTO user VALUES(1, 1, 'aa')")

        when:
        Map<Long, User> userMap = userRepo.getUsersByIdIn(Collections.singleton(1L))

        then:
        userMap.containsKey(1L)
        userMap.get(1L).authToken == "aa"
    }
}
