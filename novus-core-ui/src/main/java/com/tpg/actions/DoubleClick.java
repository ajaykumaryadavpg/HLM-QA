package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.MouseButton;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

import java.text.MessageFormat;

public class DoubleClick implements Performable {

    private final String locator;
    private double seconds = 0;
    private final NovusLoggerService log = NovusLoggerService.init(DoubleClick.class);

    private DoubleClick(String locator) {
        this.locator = locator;
    }

    public static DoubleClick on(String locator) {
        return new DoubleClick(locator);
    }

    @Override public void performAs(Actor actor) {
        actor.isWaitingFor(seconds);
        try {
            actor.usesBrowser().locator(locator).dblclick(new Locator.DblclickOptions().setButton(MouseButton.LEFT).setTimeout(DEFAULT_TIMEOUT_30000));
            log.info(MessageFormat.format("[Action Performed : DB_CLICK ] on locator : <{0}>", locator));
        } catch (PlaywrightException pe) {
            throw new NovusActionException("Double Click Action Failed on locator " + locator);
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
