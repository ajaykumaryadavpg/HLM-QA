package com.tpg.actions;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;
import java.util.Objects;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class BrowserRefresh implements Performable {

    private int times = 1;
    private Waiting condition;
    private String actionPerformed;
    private double seconds;
    private final NovusLoggerService log = NovusLoggerService.init(BrowserRefresh.class);

    public static BrowserRefresh refreshBrowser() {
        return new BrowserRefresh();
    }

    public BrowserRefresh times(int noOfTime) {
        this.times = noOfTime;
        return this;
    }

    public BrowserRefresh checking(Waiting condition, String actionPerformed) {
        this.actionPerformed = actionPerformed;
        this.condition = condition;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        for (var i = 1; i <= times; i++) {
            log.warning(MessageFormat.format("Refreshing browser {0} times", i));
            actor.usesBrowser().reload(new Page.ReloadOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setWaitUntil(WaitUntilState.LOAD));
            actor.isWaitingFor(3);
            if (Objects.nonNull(condition) && actor.is(condition)) {
                log.info(MessageFormat.format("{1} after {0} seconds", i * 5, actionPerformed));
                break;
            }
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
