package kafkaGameCoordinator.enricher

import com.google.common.collect.ImmutableSet
import kafkaGameCoordinator.enricher.models.User
import kafkaGameCoordinator.enricher.models.UserStatus
import kafkaGameCoordinator.enricher.repo.UserRepo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@SpringBootTest(classes = EnricherApplication.class, properties = "spring.main.allow-bean-definition-overriding=true")
class UserRepoTests extends Specification{

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
        jdbcTemplate.execute("INSERT INTO user_status VALUES(1, 'IDLE', 10)")

        when:
        Map<Long, User> userMap = userRepo.getUsersByIdIn(Collections.singleton(1L))

        then:
        userMap.containsKey(1L)
        userMap.get(1L).authToken == "aa"
        userMap.get(1L).status == UserStatus.IDLE
    }

    def 'should update multiple users'() {
        given: 'Multiple existing users'
        jdbcTemplate.execute("INSERT INTO user VALUES(1, 1, 'aa')")
        jdbcTemplate.execute("INSERT INTO user_status VALUES(1, 'IDLE', 10)")
        jdbcTemplate.execute("INSERT INTO user VALUES(2, 2, 'ab')")
        jdbcTemplate.execute("INSERT INTO user_status VALUES(2, 'IDLE', 100)")
        Map<Long, User> userMap = userRepo.getUsersByIdIn(ImmutableSet.of(1L, 2L))

        and: 'changes made to users'
        long now = System.currentTimeMillis()
        userMap.get(1L).status = UserStatus.FINDING
        userMap.get(1L).updatedTs = now
        userMap.get(2L).status = UserStatus.FINDING
        userMap.get(2L).updatedTs = now

        when: 'save is called'
        userRepo.saveAll(userMap.values())

        then: 'changes are saved'
        Map<Long, User> userMapSaved = userRepo.getUsersByIdIn(ImmutableSet.of(1L, 2L))
        userMapSaved.get(1L).status == UserStatus.FINDING
        userMapSaved.get(1L).updatedTs == now
        userMapSaved.get(2L).status == UserStatus.FINDING
        userMapSaved.get(2L).updatedTs == now
    }
}
