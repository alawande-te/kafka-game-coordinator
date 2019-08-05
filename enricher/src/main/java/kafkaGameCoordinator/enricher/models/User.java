package kafkaGameCoordinator.enricher.models;

public class User {
    private Long userId;
    private Long rank;
    private String authToken;
    private UserStatus status;
    private Long updatedTs;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRank() {
        return rank;
    }

    public void setRank(Long rank) {
        this.rank = rank;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Long getUpdatedTs() {
        return updatedTs;
    }

    public void setUpdatedTs(Long updatedTs) {
        this.updatedTs = updatedTs;
    }
}
