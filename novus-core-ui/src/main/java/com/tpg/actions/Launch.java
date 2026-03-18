package com.tpg.actions;

import com.microsoft.playwright.Page;
import com.tpg.actor.Actor;

public class Launch implements Performable {

    String url;
    Page.NavigateOptions configs;
    double seconds;

    private Launch(String url) {
        this.url = url;
    }

    public static Launch app(String url) {
        return new Launch(url);
    }

    public Performable withConfigs(Page.NavigateOptions configs) {
        this.configs = configs;
        return this;
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) actor.isWaitingFor(seconds);
        actor.usesBrowser().navigate(url);
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
