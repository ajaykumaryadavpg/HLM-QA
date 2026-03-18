package com.tpg.actions;


import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

import java.text.MessageFormat;

public class Enter implements Performable {

    private final String textToEnter;
    private String locator;
    private String[] ifLocator = new String[1];
    private boolean toBeIgnoredIfNotVisible = false;
    private Waiter[] waitConditions;
    private int nth = -1;
    private double seconds;
    private boolean multi = false;
    private double waitTimeForVisibility = 5;
    private final NovusLoggerService log = NovusLoggerService.init(Enter.class);

    private Enter(String textToEnter) {
        this.textToEnter = textToEnter;
    }

    public static Enter text(String a) {
        return new Enter(a);
    }

    public static Enter text(double a) {
        return new Enter(String.valueOf(a));
    }

    public static Enter text(int a) {
        return new Enter(String.valueOf(a));
    }

    public Performable after(Waiter... waitConditions) {
        this.waitConditions = waitConditions;
        return this;
    }

    public Enter on(String by) {
        this.locator = by;
        return this;
    }

    public Enter multi() {
        this.multi = true;
        return this;
    }

    public Enter nth(int index) {
        this.nth = index;
        return this;
    }

    public Performable ifDisplayed(String... locator) {
        this.ifLocator = locator;
        toBeIgnoredIfNotVisible = true;
        return this;
    }

    public Performable ifDisplayed(double waitTime, String... locator) {
        this.waitTimeForVisibility = waitTime;
        this.ifLocator = locator;
        toBeIgnoredIfNotVisible = true;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) actor.isWaitingFor(seconds);
        try {
            if (toBeIgnoredIfNotVisible) {
                ifLocator = ifLocator.length == 0 ? new String[]{locator} : ifLocator;
                var con = actor.is(Waiting.on(ifLocator[0]).seconds(waitTimeForVisibility)) && actor.usesBrowser().locator(ifLocator[0]).first().isVisible();
                if (con) enterText(actor);
                else {
                    log.warning(MessageFormat.format("[ENTER TEXT : IGNORED] on locator : <{0}>", locator));
                }
            } else if (waitConditions == null || actor.is(waitConditions)) {
                enterText(actor);
                log.info("[Action Performed : ENTER TEXT ] value : {} on locator : <{}>", textToEnter, locator);
            } else {
                log.error("[Action Failure : ENTER TEXT ] Could not enter text on : " + locator, new TimeoutError("Timed out while waiting for locator to load"));
            }
        } catch (PlaywrightException pe) {
            log.truncatedError(pe.getMessage());
            throw new NovusActionException("Text could not be entered on locator " + locator);
        }
    }

    private void enterText(Actor actor) {
        Waiting.on(locator).waitAs(actor);
        if (multi) {
            var count = Integer.parseInt(Retrieve.count().ofLocator(locator).getAs(actor));
            for (int i = 1; i <= count; i++) {
                actor.usesBrowser().locator(locator).nth(i - 1).fill(textToEnter);
            }
        } else if (nth >= 0) {
            actor.usesBrowser().locator(locator).nth(nth).fill(textToEnter);
        } else {
            actor.usesBrowser().locator(locator).first().fill(textToEnter);
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
