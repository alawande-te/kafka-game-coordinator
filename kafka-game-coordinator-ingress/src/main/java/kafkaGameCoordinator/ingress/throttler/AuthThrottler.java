package kafkaGameCoordinator.ingress.throttler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;

public class AuthThrottler implements Throttler {

    private static final Logger logger = LoggerFactory.getLogger(AuthThrottler.class);

    public static final int MAX_MESSAGES_PER_AUTH = 10;

    private final String authToken;
    private final JedisPool jedisPool;

    public AuthThrottler(String authToken, JedisPool jedisPool) {
        this.authToken = authToken;
        this.jedisPool = jedisPool;
    }

    @Override
    public boolean processOne(HttpServletRequest request) {
        String authToken = null;
        if (request.getParameter("authToken") != null) {
            authToken = request.getParameter("authToken");
        }
        else {
            return false;
        }
        try (Jedis jedis = jedisPool.getResource()) {
            int currentMinute = ((int)(System.currentTimeMillis() / 1000) % 3600);
            System.out.println(authToken+":"+currentMinute);
            return jedis.incr(authToken + ":" + currentMinute) <= MAX_MESSAGES_PER_AUTH;
        }
        catch (Exception exp) {
            logger.warn(exp.getMessage());
        }
        return false;
    }


}
