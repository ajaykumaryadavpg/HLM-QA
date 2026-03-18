package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

import java.text.MessageFormat;

public class Clear implements Performable {
    private final String locator;
    private Waiting[] waitConditions;
    private double seconds;
    private final NovusLoggerService log = NovusLoggerService.init(Clear.class);

    private Clear(String locator) {
        this.locator = locator;
    }

    public static Clear locator(String locator) {
        return new Clear(locator);
    }

    public Performable afterWaiting(Waiting... waitConditions) {
        this.waitConditions = waitConditions;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        try {
            if (waitConditions == null || actor.is(waitConditions)) {
                actor.usesBrowser().locator(locator).clear(new Locator.ClearOptions().setForce(true).setTimeout(DEFAULT_TIMEOUT_5000));
                log.info(MessageFormat.format("[Action Performed : CLEAR TEXT ] on locator : <{0}>", locator));
            } else {
                log.error("[Action Failure : CLEAR TEXT ] Could not clear text on : " + locator, new TimeoutError("timed out while waiting for locator to load"));
            }
        } catch (PlaywrightException pe) {
            log.truncatedError(pe.getMessage());
            throw new NovusActionException(pe.getMessage());
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
