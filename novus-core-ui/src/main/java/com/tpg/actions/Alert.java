package com.tpg.actions;

import com.microsoft.playwright.Dialog;
import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;

public class Alert implements Performable {

    private double seconds;
    private final String condition;
    private final NovusLoggerService log = NovusLoggerService.init(Alert.class);

    public Alert(String condition) {
        this.condition = condition;
    }

    public static Alert accept() {
        return new Alert("accept");
    }

    public static Alert dismiss() {
        return new Alert("dismiss");
    }

    @Override public void performAs(Actor actor) {
        if (seconds > 0) {
            actor.isWaitingFor(seconds);
        }
        switch (condition) {
            case "accept" -> {
                actor.usesBrowser().onDialog(Dialog::accept);
                log.info("accepted alert");
            }
            case "dismiss" -> {
                actor.usesBrowser().onDialog(Dialog::dismiss);
                log.info("dismissed alert");
            }
            default -> {
                log.error("illegal alert action", new IllegalArgumentException());
                throw new IllegalArgumentException("illegal alert action selected");
            }
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
