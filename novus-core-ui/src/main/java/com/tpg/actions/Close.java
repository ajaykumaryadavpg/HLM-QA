package com.tpg.actions;

import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Close implements Performable {

    private double seconds = 0;
    public static final NovusLoggerService log = NovusLoggerService.init(Close.class);

    public static Performable browser() {
        return new Close();
    }

    @Override public void performAs(Actor actor) {
        log.info("Closing the browser silently! Bye, Thank You!");
        if (seconds > 0) actor.isWaitingFor(seconds);
        actor.usesBrowser().close();
        actor.usesBrowser().context().close();
        actor.usesBrowser().context().browser().close();
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
