package kafkaGameCoordinator.models;

import lombok.Data;

@Data
public class EnrichedMessage {

    private Long userId;
    private Long rank;
    private Long findingSince;
    private UserStatus status;
}
