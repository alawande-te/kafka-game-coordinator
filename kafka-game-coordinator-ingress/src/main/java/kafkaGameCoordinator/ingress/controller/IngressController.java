package kafkaGameCoordinator.ingress.controller;

import kafkaGameCoordinator.ingress.throttler.AuthThrottlerFactory;
import kafkaGameCoordinator.ingress.throttler.Throttler;
import kafkaGameCoordinator.models.IngressMessage;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    private KafkaTemplate<String, IngressMessage> kafkaTemplate;

    @Autowired
    private AuthThrottlerFactory authThrottlerFactory;

    @GetMapping("/ingress")
    public String ingressEntrypoint(@RequestParam String authToken) {
        Throttler authThrottler = authThrottlerFactory.getForAuthToken(authToken);
        boolean canProceed = authThrottler.processOne();

        if (canProceed) {
            IngressMessage ingressMessage = new IngressMessage();
            ingressMessage.setAuthToken(authToken);
            ingressMessage.setTs(System.currentTimeMillis());
            kafkaTemplate.send(new ProducerRecord<>("ingress", authToken, ingressMessage));
            return "Success!";
        }
        else {
            return "Throttled!";
        }
    }

}