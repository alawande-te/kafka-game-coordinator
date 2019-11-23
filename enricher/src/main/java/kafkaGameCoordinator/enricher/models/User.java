package kafkaGameCoordinator.enricher.models;

import lombok.Data;

@Data
public class User {
    private Long userId;
    private Long rank;
    private String authToken;
    private UserStatus status;
    private Long updatedTs;
}
