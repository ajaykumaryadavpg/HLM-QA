package com.tpg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.boot.ansi.AnsiColor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NovusAnsiColors {

    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[" + AnsiColor.RED + "m";
    private static final String ANSI_YELLOW = "\u001B[" + AnsiColor.YELLOW + "m";
    private static final String ANSI_MAGENTA = "\u001B[" + AnsiColor.MAGENTA + "m";
    private static final String ANSI_BRIGHT_GREEN = "\u001B[" + AnsiColor.BRIGHT_GREEN + "m";
    private static final String ANSI_BRIGHT_BLUE = "\u001B[" + AnsiColor.BRIGHT_BLUE + "m";
    private static final String ANSI_BRIGHT_WHITE = "\u001B[" + AnsiColor.BRIGHT_WHITE + "m";
    private static final String ANSI_BLACK = "\u001B[" + AnsiColor.BLACK + "m";

    public static String step(String logMessage) {
        return ANSI_BRIGHT_BLUE + logMessage + ANSI_RESET;
    }

    public static String error(String logMessage) {
        return ANSI_RED + logMessage + ANSI_RESET;
    }

    public static String warn(String logMessage) {
        return ANSI_YELLOW + logMessage + ANSI_RESET;
    }

    public static String info(String logMessage) {
        return ANSI_MAGENTA + logMessage + ANSI_RESET;
    }

    public static String verificationSuccess(String logMessage) {
        return ANSI_BRIGHT_GREEN + logMessage + ANSI_RESET;
    }

    public static String waiter(String logMessage) {
        return ANSI_BRIGHT_WHITE + logMessage + ANSI_RESET;
    }

    public static String test(String logMessage) {
        return ANSI_BLACK + logMessage + ANSI_RESET;
    }

    public static String testFail(String logMessage) {
        return ANSI_RED + logMessage + ANSI_RESET;
    }
}
