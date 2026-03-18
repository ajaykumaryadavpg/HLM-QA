package com.tpg.actions;

import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;

public interface Waiter {
    final NovusLoggerService log = NovusLoggerService.init(Waiter.class);

    boolean waitAs(Actor actor);

    public static void waitingForSeconds(double seconds) {
        log.debug("actor waiting for " + seconds + " secs");
        try {
            Thread.sleep((long) (seconds * 1000));
        } catch (InterruptedException e) {
            log.error("Timed out ", e);
            Thread.currentThread().interrupt();
        }
    }
}
