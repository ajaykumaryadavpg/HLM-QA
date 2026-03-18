package com.tpg.actions;

import com.tpg.actor.Actor;

public interface Performable {
    static int DEFAULT_TIMEOUT_30000 = 30000;
    static int DEFAULT_TIMEOUT_5000 = 5000;

    void performAs(Actor actor);

    Performable byWaitingFor(double seconds);
}
