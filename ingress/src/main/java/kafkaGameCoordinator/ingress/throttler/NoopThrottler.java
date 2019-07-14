package kafkaGameCoordinator.ingress.throttler;

public class NoopThrottler implements Throttler {

    @Override
    public boolean processOne() {
        return true;
    }
}
