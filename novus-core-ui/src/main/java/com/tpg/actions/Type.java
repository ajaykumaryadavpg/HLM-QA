package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

import java.text.MessageFormat;

public class Type implements Performable {

    private final String textToType;
    private String locator;
    private Waiting[] waitConditions;
    private double seconds = 0;
    private boolean slowSearch = false;
    private final NovusLoggerService log = NovusLoggerService.init(Type.class);

    private Type(String textToType) {
        this.textToType = textToType;
    }

    public static Type text(String a) {
        return new Type(a);
    }

    public Performable afterWaiting(Waiting... waitConditions) {
        this.waitConditions = waitConditions;
        return this;
    }

    public Type on(String by) {
        this.locator = by;
        return this;
    }

    public Type withDelay() {
        this.slowSearch = true;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        try {
            if (waitConditions == null || actor.is(waitConditions)) {
                if (slowSearch) {
                    var lastLetter = textToType.substring(textToType.length() - 1);
                    var strToEnter = textToType.substring(0, textToType.length() - 1);
                    actor.usesBrowser().locator(locator).pressSequentially(strToEnter, new Locator.PressSequentiallyOptions().setDelay(20));
                    actor.isWaitingFor(1.5);
                    actor.usesBrowser().locator(locator).pressSequentially(lastLetter);
                } else actor.usesBrowser().locator(locator).pressSequentially(textToType, new Locator.PressSequentiallyOptions().setDelay(0.7));
                log.info(MessageFormat.format("[Action Performed : TYPE TEXT ] on locator : <{0}>", locator));
            } else {
                log.error("[Action Failure : TYPE TEXT ] Could not type text on : " + locator, new TimeoutError("timed out while waiting for locator to load"));
            }
        } catch (PlaywrightException pe) {
            log.truncatedError(pe.getMessage());
            throw new NovusActionException("Type action failed on locator : " + locator);
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }

}
