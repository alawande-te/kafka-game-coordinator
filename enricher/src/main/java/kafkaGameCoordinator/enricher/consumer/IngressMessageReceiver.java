package kafkaGameCoordinator.enricher.consumer;

import kafkaGameCoordinator.enricher.service.EnricherService;
import kafkaGameCoordinator.models.IngressMessage;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.listener.ConsumerSeekAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class IngressMessageReceiver implements ConsumerSeekAware {

    private static Logger logger = LoggerFactory.getLogger(IngressMessageReceiver.class);

    @Autowired
    EnricherService enricherService;

    @Value("${enricher.reset-offsets:false}")
    private boolean resetOffsets;

    @KafkaListener(topics = "ingress")
    public void listen(List<IngressMessage> messages) {
        logger.info(String.format("Processing %d ingress messages", messages.size()));
        enricherService.enricherUsers(messages);
    }

    @Override
    public void registerSeekCallback(ConsumerSeekCallback callback) {

    }

    @Override
    public void onPartitionsAssigned(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
        if (resetOffsets) {
            assignments.forEach((t, o) -> callback.seekToBeginning(t.topic(), t.partition()));
        }
    }

    @Override
    public void onIdleContainer(Map<TopicPartition, Long> assignments, ConsumerSeekCallback callback) {
    }
}
