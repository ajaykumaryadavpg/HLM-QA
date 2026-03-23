package com.tpg.actions;

import com.microsoft.playwright.Route;
import com.tpg.actor.Actor;
import com.tpg.exceptions.NovusActionException;
import com.tpg.services.NovusLoggerService;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.text.MessageFormat;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InterceptNetwork implements Performable {

    private String urlPattern;
    private String bodyFragment;
    private int delayMillis = 0;
    private int stubStatusCode = 0;
    private String stubBody = "";
    private boolean abort = false;
    private boolean clearAllRoutes = false;
    private double seconds = 0;
    private final NovusLoggerService log = NovusLoggerService.init(InterceptNetwork.class);

    public static InterceptNetwork matching(String urlPattern) {
        var intercept = new InterceptNetwork();
        intercept.urlPattern = urlPattern;
        return intercept;
    }

    public static InterceptNetwork clearAll() {
        var intercept = new InterceptNetwork();
        intercept.clearAllRoutes = true;
        return intercept;
    }

    public InterceptNetwork withBodyContaining(String fragment) {
        this.bodyFragment = fragment;
        return this;
    }

    public InterceptNetwork delayBy(int millis) {
        this.delayMillis = millis;
        return this;
    }

    public InterceptNetwork stubWith(int statusCode, String body) {
        this.stubStatusCode = statusCode;
        this.stubBody = body;
        return this;
    }

    public InterceptNetwork andAbort() {
        this.abort = true;
        return this;
    }

    @Override
    public void performAs(Actor actor) {
        if (seconds > 0) actor.isWaitingFor(seconds);
        if (clearAllRoutes) {
            actor.usesBrowser().unrouteAll();
            log.info("[Action Performed : INTERCEPT NETWORK] all active intercepts cleared");
            return;
        }
        try {
            actor.usesBrowser().route(urlPattern, route -> {
                if (bodyFragment != null) {
                    String postData = route.request().postData();
                    if (postData == null || !postData.contains(bodyFragment)) {
                        route.resume();
                        return;
                    }
                }
                if (delayMillis > 0) {
                    route.request().frame().page().waitForTimeout(delayMillis);
                }
                applyAction(route);
            });
            log.info(MessageFormat.format("[Action Performed : INTERCEPT NETWORK] registered for pattern: {0}", urlPattern));
        } catch (Exception e) {
            throw new NovusActionException("Failed to register network intercept for pattern: " + urlPattern);
        }
    }

    private void applyAction(Route route) {
        if (abort) {
            route.abort();
            log.debug(MessageFormat.format("[INTERCEPT NETWORK : ABORT] matched pattern: {0}", urlPattern));
        } else if (stubStatusCode > 0) {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(stubStatusCode)
                    .setBody(stubBody));
            log.debug(MessageFormat.format("[INTERCEPT NETWORK : STUB {0}] matched pattern: {1}", stubStatusCode, urlPattern));
        } else {
            route.resume();
            log.debug(MessageFormat.format("[INTERCEPT NETWORK : PASS-THROUGH] matched pattern: {0}", urlPattern));
        }
    }

    @Override
    public Performable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }
}
