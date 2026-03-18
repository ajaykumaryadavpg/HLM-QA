package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

public class CheckBox implements Performable {

    private final String locator;
    private final boolean yesNo;
    private double seconds;
    private final NovusLoggerService log = NovusLoggerService.init(CheckBox.class);

    private CheckBox(String locator, boolean yesNo) {
        this.locator = locator;
        this.yesNo = yesNo;
    }

    public static CheckBox check(String locator) {
        return new CheckBox(locator, true);
    }

    public static CheckBox uncheck(String locator) {
        return new CheckBox(locator, false);
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        try {
            if (yesNo) {
                log.info("Check locator {}", locator);
                actor.usesBrowser().locator(locator).first().check(new Locator.CheckOptions().setForce(false).setTimeout(DEFAULT_TIMEOUT_30000));
                log.info("[Action Performed : CHECK ] on locator : {}", locator);
            } else {
                log.info("Uncheck locator {}", locator);
                actor.usesBrowser().locator(locator).first().uncheck(new Locator.UncheckOptions().setForce(false).setTimeout(DEFAULT_TIMEOUT_30000));
                log.info("[Action Performed : UNCHECK ] on locator : {}", locator);
            }
        } catch (PlaywrightException pe) {
            log.truncatedError(pe.getMessage());
            throw new NovusActionException("- CHECK/UNCHECK Action Failed - /n" + pe.getMessage());
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }

}
