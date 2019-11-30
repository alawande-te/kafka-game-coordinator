package kafkaGameCoordinator.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafkaGameCoordinator.models.EnrichedMessage;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class EnrichedMessageSerializer implements Serializer<EnrichedMessage> {
    @Override
    public void configure(Map<String, ?> map, boolean b) {
    }

    @Override
    public byte[] serialize(String arg0, EnrichedMessage arg1) {
        byte[] retVal = null;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            retVal = objectMapper.writeValueAsString(arg1).getBytes();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    @Override public void close() {
    }
}
