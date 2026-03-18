package com.tpg.verification;

import com.tpg.services.NovusLoggerService;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.CanIgnoreReturnValue;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class NovusSoftAssert extends SoftAssertions {

    private Object actual;
    private Collection<Object> collection;
    private int errorsOccurred = 0;
    private String logMessage;
    private static final NovusLoggerService log = NovusLoggerService.init(NovusSoftAssert.class);

    public NovusSoftAssert() {
        super();
    }

    public NovusSoftAssert verify(String describeAs) {
        this.logMessage = describeAs;
        return this;
    }

    public NovusSoftAssert actual(Object actual) {
        this.actual = actual;
        return this;
    }

    public <T> NovusSoftAssert actualCollection(Collection<T> actual) {
        this.collection = Collections.unmodifiableCollection(actual);
        return this;
    }

    public void matches(Object expected) {
        this.assertThat(actual).as(logMessage).isEqualTo(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [%s] - Expected : [%s] Found : [%s]", logMessage, expected, actual));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [%s] - Expected : [%s] but Found : [%s]", logMessage, expected, actual));
        }
    }

    public void matches(String expected) {
        this.assertThat((String) actual).as(logMessage).isEqualToIgnoringCase(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [%s] - Expected : [%s] Found(case ignored) : [%s]", logMessage, expected, actual));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [%s] - Expected : [%s] but Found(case ignored) : [%s]", logMessage, expected, actual));
        }
    }

    public void isNotNull() {
        this.assertThat(actual).as(logMessage).isNotNull();
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [%s] - Actual value : [%s] is not null", logMessage, actual));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [%s] - Actual value : [%s] is null", logMessage, actual));
        }
    }

    public void isEmpty() {
        this.assertThat((String) actual).as(logMessage).isEmpty();
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [%s] - Actual value : [%s] is empty", logMessage, actual));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [%s] - Actual value : [%s] is not empty", logMessage, actual));
        }
    }

    public void isNotEmpty() {
        this.assertThat((String) actual).as(logMessage).isNotEmpty();
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [%s] - Actual value : [%s] is not empty", logMessage, actual));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [%s] - Actual value : [%s] is empty", logMessage, actual));
        }
    }

    @CanIgnoreReturnValue
    public NovusSoftAssert contains(String... expected) {
        this.assertThat((String) actual).as(logMessage).contains(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            var str = ((String) actual);
            if (str.length() > 200) {
                str = str.substring(0, 100);
            }
            log.verificationSuccess(String.format("soft assert : [%s] - actual full String : [%s] to contain : [%s]", logMessage, str, Arrays.toString(expected)));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual full String : [%s] to contain : [%s]", logMessage, actual, Arrays.toString(expected)));
        }
        return this;
    }

    public void contains(String expected) {
        this.assertThat((String) actual).as(logMessage).contains(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            var str = ((String) actual);
            if (str.length() > 200) {
                str = str.substring(0, 100);
            }
            log.verificationSuccess(String.format("soft assert : [%s] - actual full String : [%s] to contain : [%s]", logMessage, str, expected));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual full String : [%s] to contain : [%s]", logMessage, actual, expected));
        }
    }

    public NovusSoftAssert containsRegex(String regex) {
        this.assertThat((String) actual).as(logMessage).containsPattern(regex);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            var str = ((String) actual);
            if (str.length() > 200) {
                str = str.substring(0, 100);
            }
            log.verificationSuccess(String.format("soft assert : [%s] - actual full String : [%s] to contain : [%s]", logMessage, str, regex));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual full String : [%s] to contain : [%s]", logMessage, actual, regex));
        }
        return this;
    }

    public void doesNotContain(String... expected) {
        this.assertThat((String) actual).as(logMessage).doesNotContain(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            var str = ((String) actual);
            if (str.length() > 200) {
                str = str.substring(0, 100);
            }
            log.verificationSuccess(String.format("soft assert : [%s] - actual full String : [%s] to not contain : [%s]", logMessage, str, Arrays.toString(expected)));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual full String : [%s] to not contain : [%s]", logMessage, actual, Arrays.toString(expected)));
        }
    }

    @CanIgnoreReturnValue
    public NovusSoftAssert containsIgnoreCase(String expected) {
        this.assertThat((String) actual).as(logMessage).containsIgnoringCase(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            var str = ((String) actual);
            if (str.length() > 200) {
                str = str.substring(0, 100);
            }
            log.verificationSuccess(String.format("soft assert : [%s] - actual full String : [%s] to contain ignoring case: [%s]", logMessage, str, expected));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual full String : [%s] to contain ignoring case : [%s]", logMessage, actual, expected));
        }
        return this;
    }

    public void isIn(String... expected) {
        this.assertThat((String) actual).as(logMessage).isIn(expected);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [ %s ] - full String : [%s] contains : [%s]", logMessage, actual, expected));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - expected full String : [%s] to contain : [%s]", logMessage, actual, expected));
        }
    }

    public void sizeEquals(int expectedSize) {
        this.assertThat(collection).as(logMessage).hasSize(expectedSize);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [ %s ] - expected size of list : [%s]  matches actual size : [%s]", logMessage, expectedSize, collection.size()));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - expected size of list : [%s]  did not match actual size : [%s] in %s", logMessage, expectedSize, collection.size(), collection));
        }
    }

    public void containsAll(List<?> expectedValues) {
        this.assertThat(collection).as(logMessage).containsAll(expectedValues);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [ %s ] - actual list contains all values of expected list", logMessage));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual list doesn't contain all the values of expected list", logMessage));
        }
    }

    public void isBetween(Instant expectedDate1, Instant expectedDate2) {
        this.assertThat((Instant) actual).as(logMessage).isBetween(expectedDate1, expectedDate2);
        if (errorsOccurred == this.assertionErrorsCollected().size()) {
            log.verificationSuccess(String.format("soft assert : [ %s ] - actual date is between date range", logMessage));
        } else {
            errorsOccurred = this.assertionErrorsCollected().size();
            log.verificationFailure(String.format("soft assert : [ %s ] - actual date is not between date range", logMessage));
        }
    }

    public void verifyAllSoftAssertions() {
        this.assertAll();
    }

    public NovusSoftAssert and() {
        return this;
    }
}
