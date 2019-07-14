package kafkaGameCoordinator.ingress.throttler;

public interface Throttler {

    boolean processOne();
}
