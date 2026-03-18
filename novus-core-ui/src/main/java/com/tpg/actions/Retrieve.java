package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.options.LoadState;
import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;

public class Retrieve {

    private final String value;
    private String locator;
    private final String retrieveStrategy;
    private double seconds = 0;
    private int nth = 0;
    private final NovusLoggerService log = NovusLoggerService.init(Retrieve.class);

    private Retrieve(String value, String retrieveStrategy) {
        log.info("Retrieving using {} strategy", retrieveStrategy);
        this.value = value;
        this.retrieveStrategy = retrieveStrategy;
    }

    public Retrieve ofLocator(String locator) {
        this.locator = locator;
        return this;
    }

    public Retrieve atIndex(int index) {
        this.nth = index;
        return this;
    }

    public static Retrieve text() {
        return new Retrieve("", "TEXT");
    }

    public static Retrieve currentUrl() {
        return new Retrieve("", "CURRENT_URL");
    }

    public static Retrieve attribute(String attrName) {
        return new Retrieve(attrName, "ATTR");
    }

    public static Retrieve value() {
        return new Retrieve("", "VALUE");
    }

    public static Retrieve href() {
        return new Retrieve("href", "ATTR");
    }

    public static Retrieve inputValue() {
        return new Retrieve("", "INPUT");
    }

    public static Retrieve ifChecked() {
        return new Retrieve("", "CHECKED");
    }

    public static Retrieve count() {
        return new Retrieve("", "COUNT");
    }

    public String getAs(Actor actor) {
        actor.isWaitingFor(seconds);
        switch (retrieveStrategy) {
            case "TEXT" -> {
                return actor.usesBrowser().locator(locator).nth(nth).innerText(new Locator.InnerTextOptions().setTimeout(10000)).trim();
            }
            case "ATTR" -> {
                return actor.usesBrowser().locator(locator).nth(nth).getAttribute(value, new Locator.GetAttributeOptions().setTimeout(10000));
            }
            case "VALUE" -> {
                return actor.usesBrowser().locator(locator).first().inputValue(new Locator.InputValueOptions().setTimeout(10000));
            }
            case "CURRENT_URL" -> {
                actor.usesBrowser().waitForLoadState(LoadState.LOAD);
                actor.isWaitingFor(2);
                return (String) actor.usesBrowser().evaluate("window.location.href");
            }
            case "INPUT" -> {
                return actor.usesBrowser().locator(locator).nth(nth).inputValue();
            }
            case "COUNT" -> {
                actor.is(Waiting.on(locator));
                return String.valueOf(actor.usesBrowser().locator(locator).count());
            }
            case "CHECKED" -> {
                actor.is(Waiting.on(locator));
                return String.valueOf(actor.usesBrowser().locator(locator).isChecked(new Locator.IsCheckedOptions().setTimeout(5000)));
            }
            default -> throw new IllegalArgumentException("bad input");
        }
    }

    public Retrieve byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
