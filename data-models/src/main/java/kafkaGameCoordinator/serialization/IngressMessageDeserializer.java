package kafkaGameCoordinator.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import kafkaGameCoordinator.models.IngressMessage;
import org.apache.kafka.common.serialization.Deserializer;

import java.util.Map;

public class IngressMessageDeserializer implements Deserializer<IngressMessage> {
    @Override
    public void close() {
    }
    @Override
    public void configure(Map<String, ?> arg0, boolean arg1) {
    }
    @Override
    public IngressMessage deserialize(String arg0, byte[] arg1) {
        ObjectMapper mapper = new ObjectMapper();
        IngressMessage ingressMessage = null;
        try {
            ingressMessage = mapper.readValue(arg1, IngressMessage.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ingressMessage;
    }
}