package kafkaGameCoordinator.ingress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@RestController
public class HelloController {

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @RequestMapping("/")
    public String index() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.set("something", "nothing");
            kafkaTemplate.send("ingress", "hello", "hello");
            return jedis.get("something");
        }
    }

}