package com.tpg.locatorstrategy;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LocateBy {
    public static String id(String idString) {
        return String.format("id=%s", idString);
    }

    public static String text(String innerText) {
        return String.format("text=%s", innerText);
    }

    public static String css(String css) {
        return String.format("css=%s", css);
    }

    public static String dataIdentifier(String identifier) {
        return String.format("css=[data-identifier='%s']", identifier);
    }

    public static String xpath(String xpath) {
        return xpath;
    }

    public static String withCssText(String css, String text) {
        return String.format("%s:has-text(\"%s\")", css, text);
    }

    public static String withExactCssText(String css, String text) {
        return String.format("%s:text-is(\"%s\")", css, text);
    }

    public static String name(String name) {
        return String.format("css=[name='%s']", name);
    }
}
