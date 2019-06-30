package kafkaGameCoordinator.ingress.controller;

import kafkaGameCoordinator.ingress.throttler.AuthThrottler;
import kafkaGameCoordinator.ingress.throttler.AuthThrottlerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisPool;

@RestController
public class IngressController {

    @Autowired
    private JedisPool jedisPool;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private AuthThrottlerFactory authThrottlerFactory;

    @GetMapping("/ingress")
    public String ingressEntrypoint(@RequestParam String authToken) {
        AuthThrottler authThrottler = authThrottlerFactory.getForAuthToken(authToken);
        boolean canProceed = authThrottler.processOne();

        if (canProceed) {
            kafkaTemplate.send("ingress", "hello", "hello");
            return "Success!";
        }
        else {
            return "Throttled!";
        }
    }

}