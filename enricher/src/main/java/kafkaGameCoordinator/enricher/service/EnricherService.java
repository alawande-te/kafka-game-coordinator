package kafkaGameCoordinator.enricher.service;

import kafkaGameCoordinator.enricher.models.User;
import kafkaGameCoordinator.enricher.models.UserStatus;
import kafkaGameCoordinator.enricher.repo.UserRepo;
import kafkaGameCoordinator.models.IngressMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class EnricherService {

    @Autowired
    private UserRepo userRepo;

    public void enricherUsers(Collection<IngressMessage> ingressMessages) {
        Set<String> authTokens = ingressMessages.stream().map(IngressMessage::getAuthToken).collect(Collectors.toSet());
        Map<Long, User> dbUsers = userRepo.getUsersByAuthTokensIn(authTokens);

        Map<Long, User> findingUsers = dbUsers.values()
                .stream().filter(this::findingStatusFilter)
                .collect(Collectors.toMap(User::getUserId, Function.identity()));

        for (User user : findingUsers.values()) {
            user.setStatus(UserStatus.FINDING);
            user.setUpdatedTs(System.currentTimeMillis());
        }

        userRepo.saveAll(findingUsers.values());

        for (User user: findingUsers.values()) {
            System.out.println(user);
        }
    }

    private boolean findingStatusFilter(User user) {
        return user.getStatus() == UserStatus.FINDING ||
                user.getStatus() == UserStatus.IDLE;
    }
}
