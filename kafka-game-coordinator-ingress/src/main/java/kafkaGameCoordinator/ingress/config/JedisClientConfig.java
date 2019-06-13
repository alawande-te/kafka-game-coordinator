package kafkaGameCoordinator.ingress.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

@Configuration
public class JedisClientConfig {

    @Value("${jedis.host}")
    private String jedisHost;

    @Value("${jedis.port}")
    private String jedisPort;

    @Bean
    public Jedis jedis() {
        return new Jedis(jedisHost, Integer.valueOf(jedisPort));
    }
}
