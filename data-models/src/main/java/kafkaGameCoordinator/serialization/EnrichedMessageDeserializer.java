package kafkaGameCoordinator.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafkaGameCoordinator.models.EnrichedMessage;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class EnrichedMessageDeserializer implements Deserializer<EnrichedMessage> {
    @Override
    public void close() {
    }
    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
    }
    @Override
    public EnrichedMessage deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        EnrichedMessage enrichedMessage = null;
        try {
            enrichedMessage = mapper.readValue(arg1, EnrichedMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return enrichedMessage;
    }
}