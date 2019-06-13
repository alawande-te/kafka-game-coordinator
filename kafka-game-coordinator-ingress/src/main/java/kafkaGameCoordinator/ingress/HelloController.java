package kafkaGameCoordinator.ingress;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import redis.clients.jedis.Jedis;

@RestController
public class HelloController {

    @Autowired
    private Jedis jedis;

    @RequestMapping("/")
    public String index() {
        jedis.set("something", "nothing");
        return jedis.get("something");
    }

}