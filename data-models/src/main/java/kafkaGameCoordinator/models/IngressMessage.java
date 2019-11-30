package kafkaGameCoordinator.models;

import lombok.Data;

@Data
public class IngressMessage {

    private String authToken;
    private Long ts;

}
