package kafkaGameCoordinator.ingress.throttler;

import javax.servlet.http.HttpServletRequest;

public interface Throttler {

    boolean processOne(HttpServletRequest request);
}
