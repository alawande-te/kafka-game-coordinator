package kafkaGameCoodinator.utils

import kafkaGameCoordinator.ingress.throttler.AuthThrottler
import kafkaGameCoordinator.ingress.throttler.AuthThrottlerFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class AuthThrottlerTestConfiguration {

    private static AuthThrottler authThrottler

    @Bean
    @Primary
    AuthThrottlerFactory authThrottlerFactory() {
        return new ControllableAuthThrottlerFactory()
    }

    static class ControllableAuthThrottlerFactory extends AuthThrottlerFactory {

        @Override
        AuthThrottler getForAuthToken(String authToken) {
            authThrottler
        }
    }

    static void setMockAuthThrottler(AuthThrottler authThrottler) {
        AuthThrottlerTestConfiguration.authThrottler = authThrottler
    }
}
