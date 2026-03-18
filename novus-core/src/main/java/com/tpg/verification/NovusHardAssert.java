package com.tpg.verification;

import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;

public class NovusHardAssert<T> implements Verifiable {

    private final T obj;
    private final Matcher<? super T> objMatcher;
    private String logMessage = "";
    private final NovusLoggerService log = NovusLoggerService.init(NovusHardAssert.class);

    private NovusHardAssert(T obj, Matcher<? super T> objMatcher) {
        this.obj = obj;
        this.objMatcher = objMatcher;
    }

    public NovusHardAssert<T> describedAs(String log) {
        this.logMessage = log;
        return this;
    }

    public static <T> NovusHardAssert<T> verify(T actual, Matcher<? super T> matcher) {
        return new NovusHardAssert<>(actual, matcher);
    }

    @Override public void verifyAs(Actor actor) {
        try {
            MatcherAssert.assertThat(obj, objMatcher);
            if (obj instanceof String val && val.length() > 100) {
                log.verificationSuccess(logMessage + " - " + val.substring(0, 100) + "....." + " " + objMatcher);
            } else {
                log.verificationSuccess(logMessage + " - " + obj + " " + objMatcher);
            }
        } catch (AssertionError ase) {
            log.verificationFailure(ase.getMessage());
            throw new AssertionError(ase.getMessage());
        }
    }

    @Override public Verifiable byWaitingFor(double seconds) {
        return this;
    }
}
