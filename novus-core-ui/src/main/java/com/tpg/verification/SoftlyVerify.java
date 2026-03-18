package com.tpg.verification;

import com.tpg.annotations.Prototype;
import org.assertj.core.api.ObjectAssert;
import org.assertj.core.api.SoftAssertions;

@Prototype
public class SoftlyVerify {

    SoftAssertions softAssertions;
    String text;

    public SoftlyVerify(SoftAssertions softAssertions) {
        this.softAssertions = softAssertions;
    }

    public static SoftlyVerify verify(SoftAssertions softAssertions) {
        return new SoftlyVerify(softAssertions);
    }

    public SoftlyVerify describedAs(String log) {
        this.text = log;
        return this;
    }

    public <T> ObjectAssert<T> check(T actual) {
        return softAssertions.assertThat(actual).describedAs(text);
    }
}