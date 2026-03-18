package com.tpg.actions;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.SelectOption;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;

import java.util.Arrays;
import java.util.Objects;

public class Select implements Performable {

    private String option;
    private String[] options;
    private String locator;
    private double seconds = 0;
    private final NovusLoggerService log = NovusLoggerService.init(Select.class);

    private Select(String option) {
        this.option = option;
    }

    private Select(String... options) {
        this.options = options;
    }

    public static Select option(String option) {
        return new Select(option);
    }

    public static Select options(String... options) {
        return new Select(options);
    }

    public Select on(String selectLocator) {
        this.locator = selectLocator;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        try {
            if (Objects.nonNull(option)) {
                optionSelect(actor);
            } else if (Objects.nonNull(options) && options.length > 0) {
                optionsSelect(actor);
            } else {
                log.error("SELECT Action Failed - make sure the option is not null");
                throw new IllegalArgumentException("SELECT Action Failed - make sure the option is not null");
            }
        } catch (PlaywrightException pe) {
            log.truncatedError(pe.getMessage());
            throw new NovusActionException("SELECT Action Failed on locator: " + locator);
        }
    }

    public void optionSelect(Actor actor) {
        log.info("Selecting option from dropdown: {} ", option);
        actor.usesBrowser().locator(locator).selectOption(new SelectOption().setLabel(option), new Locator.SelectOptionOptions().setTimeout(30000));
        log.info("[Action Performed : SELECT ] on locator : {}", locator);
    }

    public void optionsSelect(Actor actor) {
        log.info("Selecting options from dropdown: " + Arrays.toString(options));
        actor.usesBrowser().locator(locator).selectOption(options, new Locator.SelectOptionOptions().setTimeout(30000));
        log.info("[Action Performed : SELECT ] on locator : {}", locator);
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
