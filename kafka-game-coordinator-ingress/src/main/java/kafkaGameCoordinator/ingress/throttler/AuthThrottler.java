package kafkaGameCoordinator.ingress.throttler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class AuthThrottler implements Throttler {

    private static final Logger logger = LoggerFactory.getLogger(AuthThrottler.class);

    private final String authToken;
    private final JedisPool jedisPool;
    private final int maxMessagesPerMinute;

    public AuthThrottler(String authToken, JedisPool jedisPool, int maxMessagesPerMinute) {
        this.authToken = authToken;
        this.jedisPool = jedisPool;
        this.maxMessagesPerMinute = maxMessagesPerMinute;
    }

    @Override
    public boolean processOne() {
        if (authToken == null) {
            return false;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            int currentMinute = ((int)(System.currentTimeMillis() / 1000) % 3600);
            System.out.println(authToken+":"+currentMinute);
            return jedis.incr(authToken + ":" + currentMinute) <= maxMessagesPerMinute;
        }
        catch (Exception exp) {
            logger.warn(exp.getMessage());
        }
        return false;
    }


}
