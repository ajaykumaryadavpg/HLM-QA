package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.ElementState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.tpg.actor.Actor;

import java.util.Objects;

public class Waiting implements Waiter {
    private final String locator;
    private double timeOut = 30;
    private final Page.WaitForSelectorOptions waitForSelectorOptions;
    private ElementState state;
    private int nth = 0;

    private Waiting(String locator) {
        this.locator = locator;
        waitForSelectorOptions = new Page.WaitForSelectorOptions();
    }

    public Waiting seconds(double timeOut) {
        this.timeOut = timeOut;
        return this;
    }

    public static Waiting on(String locator) {
        return new Waiting(locator);
    }

    public Waiting toBe(WaitForSelectorState state) {
        waitForSelectorOptions.setState(state);
        return this;
    }

    public Waiting nth(int nth) {
        this.nth = nth;
        return this;
    }

    public Waiting withState(ElementState state) {
        this.state = state;
        return this;
    }

    public Waiting within(double seconds) {
        waitForSelectorOptions.setTimeout(seconds * 1000);
        return this;
    }

    public boolean waitAs(Actor actor) {
        try {
            if (Objects.isNull(waitForSelectorOptions.state)) {
                log.wait("Waiting for locator : {} to be visible in {} sec", locator, timeOut);
                actor.usesBrowser().waitForLoadState();
                actor.usesBrowser().locator(locator).nth(nth).waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE).setTimeout(timeOut * 1000));
            } else {
                log.info("Waiting on locator : {} to have state : {}", locator, waitForSelectorOptions.state);
                if (Objects.isNull(state)) actor.usesBrowser().waitForSelector(locator, waitForSelectorOptions);
                else actor.usesBrowser().waitForSelector(locator, waitForSelectorOptions).waitForElementState(state);
            }
            log.info("Locator : {} visible on GUI", locator);
            return true;
        } catch (TimeoutError te) {
            log.warning("[Timed Out] waiting for element {}", locator);
            return false;
        }
    }
}
