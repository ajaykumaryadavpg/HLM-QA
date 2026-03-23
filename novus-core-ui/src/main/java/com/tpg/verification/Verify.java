package com.tpg.verification;

import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PageAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.tpg.actor.Actor;
import com.tpg.services.NovusLoggerService;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.opentest4j.AssertionFailedError;

import java.util.regex.Pattern;

public class Verify<T> implements Verifiable {

    private String locator;
    private String value;
    private String name;
    private VerificationStrategy strategy;
    private T obj;
    private Matcher<? super T> objMatcher;
    private String logMessage = "";
    private double seconds = 0;
    private final NovusLoggerService log = NovusLoggerService.init(Verify.class);

    private Verify(String locator) {
        this.locator = locator;
    }

    private Verify(T obj, Matcher<? super T> objMatcher) {
        this.strategy = VerificationStrategy.OBJECT;
        this.obj = obj;
        this.objMatcher = objMatcher;
    }

    private Verify() {
    }

    public static Verify<Object> uiElement(String locator) {
        return new Verify<>(locator);
    }

    public static Verify<Object> page() {
        return new Verify<>();
    }

    public Verify<T> url() {
        strategy = VerificationStrategy.URL;
        return this;
    }

    public Verifiable hasId(String value) {
        strategy = VerificationStrategy.ID;
        this.value = value;
        return this;
    }

    public Verifiable hasClass(String value) {
        strategy = VerificationStrategy.CLASS;
        this.value = value;
        return this;
    }

    public Verify<T> title() {
        strategy = VerificationStrategy.PAGE_TITLE;
        return this;
    }

    public Verifiable contains(String value) {
        this.value = value;
        return this;
    }


    public Verifiable containsText(String value) {
        strategy = VerificationStrategy.CONTAINS_TEXT;
        this.value = value;
        return this;
    }

    public Verifiable doesNotContainText(String value) {
        strategy = VerificationStrategy.DOES_NOT_CONTAIN_TEXT;
        this.value = value;
        return this;
    }

    public Verifiable hasText(String value) {
        strategy = VerificationStrategy.MATCHES_TEXT;
        this.value = value;
        return this;
    }

    public Verifiable isVisible() {
        strategy = VerificationStrategy.LOCATOR_VISIBLE;
        return this;
    }

    public Verifiable isNotVisible() {
        strategy = VerificationStrategy.LOCATOR_NOT_VISIBLE;
        return this;
    }

    public Verifiable isDisabled() {
        strategy = VerificationStrategy.IS_DISABLED;
        return this;
    }

    public Verifiable hasCSS(String name, String value) {
        strategy = VerificationStrategy.CSS;
        this.value = value;
        this.name = name;
        return this;
    }

    public static <T> Verify<T> verify(T actual, Matcher<? super T> matcher) {
        return new Verify<>(actual, matcher);
    }

    public Verify<T> describedAs(String log) {
        this.logMessage = log;
        return this;
    }

    @Override
    public Verifiable byWaitingFor(double seconds) {
        this.seconds = seconds;
        return this;
    }

    private String truncate(String msg) {
        if (msg == null || msg.length() <= 1) return msg == null ? "" : msg;
        return msg.substring(0, Math.min(msg.length(), 150));
    }

    @Override public void verifyAs(Actor actor) {
        switch (strategy) {
            case ID -> PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).hasId(value, new LocatorAssertions.HasIdOptions().setTimeout(5000));
            case CLASS -> PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).hasClass(value, new LocatorAssertions.HasClassOptions().setTimeout(5000));
            case CSS -> PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).hasCSS(name, value, new LocatorAssertions.HasCSSOptions().setTimeout(5000));
            case PAGE_TITLE -> {
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser()).hasTitle(Pattern.compile("(" + value + ")"), new PageAssertions.HasTitleOptions().setTimeout(5000));
                    log.verificationSuccess("page title matched regex");
                } catch (AssertionFailedError afe) {
                    log.verificationFailure(truncate(afe.getMessage()));
                }
            }
            case LOCATOR_VISIBLE -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator).first()).isVisible(new LocatorAssertions.IsVisibleOptions().setTimeout(30000));
                    log.verificationSuccess(locator + " is visible on UI");
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case LOCATOR_NOT_VISIBLE -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator).first()).isHidden(new LocatorAssertions.IsHiddenOptions().setTimeout(30000));
                    log.verificationSuccess(locator + " is not visible on UI");
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case IS_DISABLED -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).isDisabled(new LocatorAssertions.IsDisabledOptions().setTimeout(30000));
                    log.verificationSuccess(locator + " is disabled on UI");
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }

            case CONTAINS_TEXT -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).containsText(value, new LocatorAssertions.ContainsTextOptions().setUseInnerText(true).setTimeout(5000));
                    log.verificationSuccess(locator + " contains inner text " + value);
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case DOES_NOT_CONTAIN_TEXT -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).not().containsText(value, new LocatorAssertions.ContainsTextOptions().setUseInnerText(true).setTimeout(5000));
                    log.verificationSuccess(locator + " doesn not contain inner text " + value);
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case MATCHES_TEXT -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser().locator(locator)).hasText(value, new LocatorAssertions.HasTextOptions().setUseInnerText(true).setTimeout(5000));
                    log.verificationSuccess(locator + " has inner text " + value);
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case URL -> {
                actor.isWaitingFor(seconds);
                try {
                    PlaywrightAssertions.assertThat(actor.usesBrowser()).hasURL(Pattern.compile("(" + value + ")"), new PageAssertions.HasURLOptions().setTimeout(5000));
                    log.verificationSuccess(actor.usesBrowser().url() + " contains partial text " + value);
                } catch (AssertionFailedError afe) {
                    String msg = truncate(afe.getMessage());
                    log.verificationFailure(msg);
                    throw new AssertionError(msg);
                }
            }
            case OBJECT -> {
                try {
                    MatcherAssert.assertThat(obj, objMatcher);
                    if (obj instanceof String val && val.length() > 100) {
                        log.verificationSuccess(logMessage + " - " + val.substring(0, 100) + "....." + " " + objMatcher.toString());
                    } else {
                        log.verificationSuccess(logMessage + " - " + obj + " " + objMatcher.toString());
                    }
                } catch (AssertionError ase) {
                    log.verificationFailure(ase.getMessage());
                    throw new AssertionError(ase.getMessage());
                }
            }
            default -> throw new IllegalArgumentException("bad verification strategy");
        }

    }

}