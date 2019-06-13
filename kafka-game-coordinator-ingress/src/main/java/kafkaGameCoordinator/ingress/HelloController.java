package kafkaGameCoordinator.ingress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class HelloController {

    @Autowired
    private JedisPool jedisPool;

    @RequestMapping("/")
    public String index() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("something", "nothing");
            return jedis.get("something");
        }
    }

}