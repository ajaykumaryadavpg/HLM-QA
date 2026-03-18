package com.tpg.utils;

import com.tpg.services.NovusLoggerService;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class Retry {

    private final Runnable action;
    private Runnable otherwise;
    private Runnable meanwhile;
    private boolean retryException = false;
    private int times;
    private Class<? extends Throwable>[] exceptions;
    private final NovusLoggerService log = NovusLoggerService.init(Retry.class);

    private Retry(Runnable action) {
        this.action = action;
    }

    public static Retry action(Runnable action) {
        return new Retry(action);
    }

    public Retry times(int numberOfRetries) {
        this.times = numberOfRetries;
        return this;
    }

    public Retry untilExceptionEncountered() {
        this.retryException = true;
        return this;
    }

    @SafeVarargs public final Retry ignoring(Class<? extends Throwable>... exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    public Retry meanwhilePerform(Runnable runMeanwhile) {
        this.meanwhile = runMeanwhile;
        return this;
    }

    public void otherwisePerform(Runnable runOtherwise) {
        this.otherwise = runOtherwise;
    }

    public void run() {
        int count = 1;
        for (int i = 1; i <= times; i++) {
            log.info("Retry number {}", i);
            try {
                action.run();
                if (!retryException) break;
            } catch (Exception ex) {
                if (Objects.nonNull(exceptions) && Arrays.stream(exceptions).anyMatch(e -> e.isInstance(ex))) {
                    log.info("Encountered exception {} : {}", ex.getClass().getSimpleName(), ex.getMessage());
                    Optional.ofNullable(otherwise).ifPresent(Runnable::run);
                } else {
                    log.info("caught exception [ {} ] was not ignored. Hence retry stopped after {} retries", ex.getClass().getSimpleName(), i);
                    break;
                }
            }
            Optional.ofNullable(meanwhile).ifPresent(Runnable::run);
            count = i;
        }
        log.info("Retry Action was complete after {} retries", count);
    }
}
