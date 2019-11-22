package kafkaGameCoordinator.enricher.consumer;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.KafkaListener;

@Configuration
@EnableKafka
public class IngressMessageConsumer {

    @KafkaListener(topics = "ingress", groupId = "EnricherApp")
    public void listen(String message) {
        System.out.println("Received Messasge in group foo: " + message);
    }
}
