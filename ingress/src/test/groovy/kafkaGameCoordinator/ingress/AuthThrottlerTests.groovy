package kafkaGameCoordinator.ingress

import kafkaGameCoordinator.ingress.throttler.AuthThrottler
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import spock.lang.Specification
import spock.lang.Unroll

@Unroll
class AuthThrottlerTests extends Specification {

    def 'should not throttle auth requests if not reached limit for minute'() {
        given: 'AuthThrottlers for authTokens'
        JedisPool jedisPool = Mock(JedisPool)
        Jedis jedis = Mock(Jedis)
        jedisPool.getResource() >> jedis
        AuthThrottler authThrottler = new AuthThrottler("one", jedisPool, 10)

        when:
        jedis.incr(_ as String) >> throttlerResponse
        boolean wasProcessed = authThrottler.processOne()

        then:
        wasProcessed

        where:
        throttlerResponse << [1, 2, 5, 10]
    }

    def 'should throttle auth requests if reached limit for minute'() {
        given: 'AuthThrottlers for authTokens'
        JedisPool jedisPool = Mock(JedisPool)
        Jedis jedis = Mock(Jedis)
        jedisPool.getResource() >> jedis
        AuthThrottler authThrottler = new AuthThrottler("one", jedisPool, 10)

        when:
        jedis.incr(_ as String) >> throttlerResponse
        boolean wasProcessed = authThrottler.processOne()

        then:
        !wasProcessed

        where:
        throttlerResponse << [11, 20]
    }
}
