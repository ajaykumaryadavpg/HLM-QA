package com.tpg.actions;

import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusConfigException;
import com.tpg.services.NovusLoggerService;
import com.tpg.utils.Retry;

import java.util.Objects;
import java.util.Optional;

public class Perform implements Performable {

    private final Performable[] taskList;
    private static final NovusLoggerService log = NovusLoggerService.init(Perform.class);
    private String logMessage = "";
    private String callingMethod = "";
    private double wait;
    private Runnable meanwhileAction = null;
    private Performable[] otherwise = null;
    private int count = 0;
    private Class<? extends Throwable>[] exceptions;
    private String ifLocator = null;

    private Perform(Performable... tasks) {
        this.taskList = tasks;
    }

    public static Perform actions(Performable... tasks) {
        return new Perform(tasks);
    }

    public Perform iff(String locator) {
        this.ifLocator = locator;
        return this;
    }

    public Perform isPresent() {
        return this;
    }

    public Performable log(String callingMethod, String log) {
        this.logMessage = log;
        this.callingMethod = callingMethod;
        return this;
    }

    public Perform twice() {
        this.count = 2;
        return this;
    }

    public Perform thrice() {
        this.count = 3;
        return this;
    }

    @SafeVarargs
    public final Perform ifExceptionOccurs(Class<? extends Throwable>... exceptions) {
        this.exceptions = exceptions;
        return this;
    }

    public Perform then(Performable... actions) {
        this.otherwise = actions;
        return this;
    }

    public Perform meanwhile(Runnable action) {
        this.meanwhileAction = action;
        return this;
    }

    @Override public void performAs(Actor actor) {
        actor.isWaitingFor(wait);
        if (!logMessage.isEmpty()) {
            log.step(String.format("[ method : %s ] - %s ", callingMethod, logMessage));
        } else throw new NovusConfigException("please add a log after calling the \"actions\" method ");
        if (Objects.isNull(ifLocator) || actor.is(Waiting.on(ifLocator).seconds(10))) {
            if (count == 0) actor.attemptsTo(taskList);
            else {
                var retry = Retry.action(() -> actor.attemptsTo(taskList))
                    .times(count)
                    .ignoring(exceptions);
                if (Objects.nonNull(otherwise)) retry.otherwisePerform(() -> actor.attemptsTo(otherwise));
                Optional.ofNullable(meanwhileAction).ifPresentOrElse(m -> retry.meanwhilePerform(m).run(), retry::run);
            }
        } else {
            log.warning("Condition check failed for locator : {} so subsequent actions were not performed", ifLocator);
        }
    }

    @Override public Performable byWaitingFor(double seconds) {
        this.wait = seconds;
        return this;
    }
}
