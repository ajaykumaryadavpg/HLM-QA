package com.tpg.actions;

import com.microsoft.playwright.Browser;
import com.tpg.actor.Actor;
import com.tpg.configs.TestConfigs;
import com.tpg.services.NovusLoggerService;
import com.tpg.utils.AppContextManager;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Open implements Performable {

    private double seconds = 0;
    private static final NovusLoggerService log = NovusLoggerService.init(Open.class);


    @Override public void performAs(Actor actor) {
        log.info("Hi! Opening a new browser");
        var config = AppContextManager.getAppContext().getBean(TestConfigs.class);
        if (seconds > 0) actor.isWaitingFor(seconds);
        var newBrowserWindow = actor.usesBrowser().context().browser().newContext(new Browser.NewContextOptions().setViewportSize(config.getWidth(), config.getHeight()));
        actor.setBrowser(newBrowserWindow.newPage());
    }

    public static Performable aNewBrowser() {
        return new Open();
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
