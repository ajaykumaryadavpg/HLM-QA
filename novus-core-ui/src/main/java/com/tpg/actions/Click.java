package com.tpg.actions;


import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.TimeoutError;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;
import com.tpg.utils.Retry;
import org.testng.Assert;

import java.text.MessageFormat;
import java.util.Optional;

public class Click implements Performable {
    private final String locator;
    private String[] ifLocator = new String[1];
    private String acceptLocator = null;
    private Waiting[] beforeWaitConditions;
    private Waiting[] afterWaitConditions;
    private int nth = 0;
    private String frame;
    private boolean isFrame;
    private boolean toBeIgnoredIfNotVisible = false;
    private boolean toBeIgnoredIfVisible = false;
    private boolean untilCheckEnabled = false;
    private double seconds = 0;
    private boolean multi;
    private int retryTimes = 0;
    private Performable[] action = null;
    private double waitTimeForVisibility = 5;
    private String untilLocator;
    private final NovusLoggerService log = NovusLoggerService.init(Click.class);

    public Click afterWaiting(Waiting... waitConditions) {
        this.beforeWaitConditions = waitConditions;
        return this;
    }

    public Click and() {
        return this;
    }

    public Click laterWaiting(Waiting... waitConditions) {
        this.afterWaitConditions = waitConditions;
        return this;
    }

    private Click(String locator) {
        this.locator = locator;
    }

    public static Click on(String locator) {
        return new Click(locator);
    }

    public Click nth(int nth) {
        this.nth = nth;
        return this;
    }

    public Click multipleTimes() {
        this.multi = true;
        return this;
    }

    public Click accept(String acceptLocator) {
        this.acceptLocator = acceptLocator;
        return this;
    }

    public Click last() {
        this.nth = 100000;
        return this;
    }

    public Click ifDisplayed(String... locator) {
        this.ifLocator = locator;
        toBeIgnoredIfNotVisible = true;
        return this;
    }

    public Click ifDisplayed(double waitTime, String... locator) {
        this.waitTimeForVisibility = waitTime;
        this.ifLocator = locator;
        toBeIgnoredIfNotVisible = true;
        return this;
    }

    public Click bySwitchingToFrame(String frameName) {
        this.frame = frameName;
        isFrame = true;
        return this;
    }

    public Click retryTimes(int times) {
        this.retryTimes = times;
        return this;
    }

    public Click ifNotDisplayed(String... locator) {
        this.ifLocator = locator;
        toBeIgnoredIfVisible = true;
        return this;
    }

    public Click orElse(Performable... action) {
        this.action = action;
        return this;
    }

    public Click until(String locator) {
        this.untilLocator = locator;
        this.untilCheckEnabled = true;
        return this;
    }

    public Click isVisible() {
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (beforeWaitConditions != null && !actor.is(beforeWaitConditions)) {
            throw new TimeoutError(String.format("Click Action Failed on %s as before wait condition failed", locator));
        }
        if (isFrame) {
            var framee = actor.usesBrowser().frame(frame);
            log.debug("[Action Performed : FRAME SWITCH ] on " + frame);
            framee.locator(locator).nth(nth).click();
            log.info(MessageFormat.format("[Action Performed : CLICK ] on locator : <{0}>", locator));
            return;
        }
        if (toBeIgnoredIfNotVisible) {
            ifLocator = ifLocator.length == 0 ? new String[]{locator} : ifLocator;
            var con = actor.is(Waiting.on(ifLocator[0]).seconds(waitTimeForVisibility).nth(nth)) && actor.usesBrowser().locator(ifLocator[0]).nth(nth).isVisible();
            if (con) checkMulti(actor);
            else {
                log.warning(MessageFormat.format("[CLICK : IGNORED] on locator : <{0}>", locator));
                Optional.ofNullable(action).ifPresent(actor::attemptsTo);
                return;
            }
        } else if (toBeIgnoredIfVisible) {
            var con = actor.is(Waiting.on(ifLocator[0]).seconds(waitTimeForVisibility)) && actor.usesBrowser().locator(ifLocator[0]).first().isVisible();
            if (!con) checkMulti(actor);
            else {
                log.warning(MessageFormat.format("[CLICK : IGNORED] on locator : <{0}>", locator));
                return;
            }
        } else checkMulti(actor);
        if (afterWaitConditions != null) {
            Assert.assertTrue(actor.is(afterWaitConditions), "after wait condition failed");
        }
    }

    private void checkMulti(Actor actor) {
        if (multi) {
            var total = actor.usesBrowser().locator(locator).count();
            log.debug("total encountered elements :  " + total);
            if (total == 0) {
                log.warning(MessageFormat.format("[CLICK : IGNORED] on {0} as matching locator is 0", locator));
                return;
            }
            while (total > 0) {
                performClick(actor);
                Optional.ofNullable(acceptLocator).ifPresent(l -> Click.on(l).byWaitingFor(1));
                total--;
                log.debug("remaining locators : " + total);
            }
        } else {
            performClick(actor);
        }
    }

    private void performClick(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        try {
            if (nth == 100000) {
                actor.usesBrowser().locator(locator).last().click();
            } else if (nth != 0) {
                actor.usesBrowser().locator(locator).nth(nth).click();
            } else {
                if (retryTimes > 0)
                    Retry.action(() -> actor.usesBrowser().locator(locator).first().click())
                        .times(retryTimes)
                        .untilExceptionEncountered()
                        .meanwhilePerform(() -> actor.isWaitingFor(0.5))
                        .run();
                else if (untilCheckEnabled) {
                    for (int i = 0; i < 10; i++) {
                        actor.usesBrowser().locator(locator).first().click();
                        if (actor.is(Waiting.on(untilLocator))) {
                            break;
                        }
                    }
                } else {
                    actor.usesBrowser().locator(locator).first().click();
                    actor.usesBrowser().onConsoleMessage(msg -> {
                        if ("error".equalsIgnoreCase(msg.type())) log.debug("Console Message : " + msg.type() + " : " + msg.text());
                    });
                }
                log.info(MessageFormat.format("[Action Performed : CLICK ] on locator : <{0}>", locator));
            }
        } catch (PlaywrightException e) {
            log.error(e.getMessage());
            actor.usesBrowser().onConsoleMessage(msg -> {
                if ("error".equalsIgnoreCase(msg.type())) log.debug("Console Message : " + msg.type() + " : " + msg.text());
            });
            throw new NovusActionException("Click Action Failed on locator : " + locator);
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
