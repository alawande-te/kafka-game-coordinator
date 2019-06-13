package kafkaGameCoordinator.ingress

import kafkaGameCoordinator.ingress.throttler.AuthThrottler
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import spock.lang.Specification
import spock.lang.Unroll

import javax.servlet.http.HttpServletRequest

@Unroll
class AuthThrottlerTests extends Specification {

    def 'should not throttle auth requests if not reached limit for minute'() {
        given: 'AuthThrottlers for authTokens'
        JedisPool jedisPool = Mock(JedisPool)
        Jedis jedis = Mock(Jedis)
        jedisPool.getResource() >> jedis
        AuthThrottler authThrottler = new AuthThrottler("one", jedisPool)

        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter("authToken") >> "one"

        when:
        jedis.incr(_ as String) >> throttlerResponse
        boolean wasProcessed = authThrottler.processOne(request)

        then:
        wasProcessed

        where:
        throttlerResponse << [1, 2, 5, AuthThrottler.MAX_MESSAGES_PER_AUTH]
    }

    def 'should throttle auth requests if reached limit for minute'() {
        given: 'AuthThrottlers for authTokens'
        JedisPool jedisPool = Mock(JedisPool)
        Jedis jedis = Mock(Jedis)
        jedisPool.getResource() >> jedis
        AuthThrottler authThrottler = new AuthThrottler("one", jedisPool)

        HttpServletRequest request = Mock(HttpServletRequest)
        request.getParameter("authToken") >> "one"

        when:
        jedis.incr(_ as String) >> throttlerResponse
        boolean wasProcessed = authThrottler.processOne(request)

        then:
        !wasProcessed

        where:
        throttlerResponse << [AuthThrottler.MAX_MESSAGES_PER_AUTH+1,
                              AuthThrottler.MAX_MESSAGES_PER_AUTH*2]
    }
}
