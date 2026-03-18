package com.tpg.actions;

import com.microsoft.playwright.PlaywrightException;
import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;

public class Keyboard implements Performable {

    private String locator;
    private final String key;
    private int times = 1;
    private double seconds;
    private final NovusLoggerService log = NovusLoggerService.init(Keyboard.class);

    private Keyboard(String key) {
        this.key = key;
    }

    public static Keyboard press(String key) {
        return new Keyboard(key);
    }

    public Keyboard on(String locator) {
        this.locator = locator;
        return this;
    }

    public Performable times(int times) {
        this.times = times;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        for (int i = 0; i < times; i++) {
            try {
                actor.usesBrowser().locator(locator).press(key);
            } catch (PlaywrightException pe) {
                log.warning("[Action Ignored : Keyboard Press ]");
                log.truncatedError(pe.getMessage());
            }
        }
        log.info("[Action Performed : Keyboard Press ] on locator <{}>", locator);
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
