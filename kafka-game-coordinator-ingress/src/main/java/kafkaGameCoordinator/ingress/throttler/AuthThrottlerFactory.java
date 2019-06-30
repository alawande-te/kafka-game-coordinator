package kafkaGameCoordinator.ingress.throttler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;

import java.util.HashMap;
import java.util.Map;

@Component
public class AuthThrottlerFactory {

    @Autowired
    private JedisPool jedisPool;

    @Value("${auth.max}")
    private String maxMessagesPerMinute;

    private final Map<String, AuthThrottler> authThrottlerMap = new HashMap<>();

    public AuthThrottler getForAuthToken(String authToken) {
        return authThrottlerMap.containsKey(authToken) ?
                    authThrottlerMap.get(authToken) : createAuthThrottler(authToken);
    }

    private AuthThrottler createAuthThrottler(String authToken) {
        AuthThrottler authThrottler = new AuthThrottler(authToken, jedisPool, Integer.valueOf(maxMessagesPerMinute));
        authThrottlerMap.put(authToken, authThrottler);
        return authThrottler;
    }
}
