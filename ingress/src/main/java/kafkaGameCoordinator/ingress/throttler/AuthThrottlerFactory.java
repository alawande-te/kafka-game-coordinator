package kafkaGameCoordinator.ingress.throttler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthThrottlerFactory {
    private static Logger logger = LoggerFactory.getLogger(AuthThrottlerFactory.class);
    private boolean throttlingEnabled = true;
    private Throttler noopThrottler = new NoopThrottler();

    @Autowired
    private JedisPool jedisPool;

    @Value("${auth.max}")
    private String maxMessagesPerMinute;

    @PostConstruct
    void init() {
        try (Jedis jedis = jedisPool.getResource()) {
        }
        catch (Exception exp) {
            logger.warn("Could not initialize redis. Throttling disabled");
            throttlingEnabled = false;
        }
    }

    private final Map<String, AuthThrottler> authThrottlerMap = new HashMap<>();

    public Throttler getForAuthToken(String authToken) {
        if (!throttlingEnabled) {
            return noopThrottler;
        }
        return authThrottlerMap.containsKey(authToken) ?
                    authThrottlerMap.get(authToken) : createAuthThrottler(authToken);
    }

    private AuthThrottler createAuthThrottler(String authToken) {
        AuthThrottler authThrottler = new AuthThrottler(authToken, jedisPool, Integer.valueOf(maxMessagesPerMinute));
        authThrottlerMap.put(authToken, authThrottler);
        return authThrottler;
    }
}
