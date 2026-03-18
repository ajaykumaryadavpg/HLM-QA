package com.tpg.services;

import com.tpg.utils.AppContextManager;
import com.tpg.utils.NovusAnsiColors;
import org.apache.logging.log4j.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

public class NovusLoggerService {
    Logger log;

    private NovusLoggerService(Class<?> clazz) {
        log = LoggerFactory.getLogger(clazz);
    }

    public static NovusLoggerService init(Class<?> clazz) {
        return new NovusLoggerService(clazz);
    }

    public void step(String step, Object... obj) {
        log.info(NovusAnsiColors.step(MessageFormat.format("[Executing Step] : {0}", step)), obj);
    }

    public void info(String message) {
        log.info(NovusAnsiColors.info(message));
    }

    public void info(String message, Object... obj) {
        log.info(NovusAnsiColors.info(message), obj);
    }

    public void debug(String message, Object... obj) {
        log.debug(NovusAnsiColors.info(message), obj);
    }

    public void test(String message) {
        var totalSpaces = 150;
        var left = totalSpaces - (message.length() + 15);
        int leftSpace = left / 2;
        int rightSpace = Math.abs(left - leftSpace);
        String builder = "\n-" + Strings.repeat("=", 150) + "-" + "\n|" +
                         Strings.repeat(" ", leftSpace) +
                         "Running test : " + message +
                         Strings.repeat(" ", rightSpace) +
                         "|" +
                         "\n-" + Strings.repeat("=", 150) + "-";
        log.info(NovusAnsiColors.test(builder));
    }

    public synchronized void verificationSuccess(String successMessage) {
        var nrs = AppContextManager.getAppContext().getBean(NovusReportingService.class);
        nrs.pass(successMessage);
        log.info(NovusAnsiColors.verificationSuccess("[Verification : SUCCESS] : " + successMessage.replace("\r", "")));
    }

    public synchronized void verificationFailure(String failureMessage) {
        var nrs = AppContextManager.getAppContext().getBean(NovusReportingService.class);
        nrs.fail(failureMessage);
        log.error(NovusAnsiColors.error("[Verification : FAILURE] : " + failureMessage));
    }

    public void wait(String waiter, Object... obj) {
        log.debug(NovusAnsiColors.waiter("[Dynamic Wait] : " + waiter), obj);
    }


    public void error(String error, Throwable th) {
        log.error(NovusAnsiColors.error(MessageFormat.format("[Exception occurred] {0} with message {1}", error, th)));
    }

    public void error(String error) {
        log.error(NovusAnsiColors.error(MessageFormat.format("[Exception occurred] with message {0} ", error)));
    }

    public void testPass(String message) {
        log.info(NovusAnsiColors.verificationSuccess(message));
    }

    public void testFail(String message) {
        log.error(NovusAnsiColors.testFail(message));
    }

    public void testSkip(String message) {
        log.error(NovusAnsiColors.warn(message));
    }

    public void truncatedError(String error) {
        var message = "";
        if (error.length() > 200)
            message = error.substring(0, 199);
        else
            message = error;
        log.error(NovusAnsiColors.error(MessageFormat.format("[Exception occurred] with message : {0} ..... error truncated", NovusAnsiColors.error(message))));
    }

    public void warning(String message, Object... obj) {
        log.warn(NovusAnsiColors.warn(MessageFormat.format("[Warning] : {0}", message)), obj);
    }

    public void skip(String message) {
        log.warn(NovusAnsiColors.warn(message));
    }

    public void fail(String message) {
        log.warn(NovusAnsiColors.error(message));
    }

    public void pass(String message) {
        log.warn(NovusAnsiColors.verificationSuccess(message));
    }

    public void uiException(String error) {
        log.error(NovusAnsiColors.error(MessageFormat.format("[UI Exception occurred] : Exception {0} ", error)));
    }
}
