package com.tpg.verification;

import com.tpg.actor.Actor;

public interface Verifiable {

    void verifyAs(Actor actor);

    Verifiable byWaitingFor(double seconds);
}
