package com.tpg.automation.listeners;

import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.services.NovusLoggerService;
import com.tpg.utils.NovusTest;
import org.apache.logging.log4j.util.Strings;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NovusTestListener implements ITestListener {

    private static final NovusLoggerService log = NovusLoggerService.init(NovusTestListener.class);
    protected static final Map<String, Set<NovusTest>> testMap = new HashMap<>();
    private static final Set<NovusTest> tests = new HashSet<>();

    @Override public void onStart(ITestContext context) {
        log.info(String.format(""" 
            Running test suite : %s
            Test Name : %s
            """, context.getCurrentXmlTest().getSuite().getName(), context.getCurrentXmlTest().getName()));
    }

    @Override public void onTestStart(ITestResult result) {
        log.test(result.getName());
    }

    @Override public void onTestSuccess(ITestResult result) {
        log.testPass(Strings.repeat("-", 5) + ".: Test passed :." + Strings.repeat("-", 5));
        addTestResult(result, "passed");
    }

    @Override public void onTestFailure(ITestResult result) {
        log.testFail(Strings.repeat("-", 5) + ".: Test failed :." + Strings.repeat("-", 5));
        addTestResult(result, "failed");
    }

    @Override public void onTestSkipped(ITestResult result) {
        log.testSkip(Strings.repeat("-", 5) + ".: Test skipped :." + Strings.repeat("-", 5));
        addTestResult(result, "skipped");
    }

    @Override public void onFinish(ITestContext context) {
        log.info("- Tests Passed -");
        context.getPassedTests().getAllMethods()
            .forEach(f -> log.pass(f.getMethodName()));
        log.info("- Tests Failed -");
        context.getFailedTests().getAllMethods()
            .forEach(f -> log.fail(f.getMethodName()));
        log.info("- Tests Skipped -");
        context.getSkippedTests().getAllMethods()
            .forEach(f -> log.skip(f.getMethodName()));
        createResultMap();
        printTestTable();
    }

    private void addTestResult(ITestResult result, String status) {
        NovusTest novusTest = new NovusTest();
        Set<NovusTest> l = new HashSet<>();
        var metaData = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(MetaData.class);
        var description = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(Description.class);
        novusTest.setScenario(description.value());
        novusTest.setStatus(status);
        novusTest.setCategory(metaData.category());
        tests.add(novusTest);
        testMap.put(novusTest.getCategory(), l);
    }

    private void printTestTable() {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Set<NovusTest>> map : testMap.entrySet()) {
            builder.append("\nCategory : ").append(map.getKey());
            for (NovusTest novusTest : map.getValue()) {
                builder.append(String.format("%n| %s | %s |", novusTest.getScenario(), novusTest.getStatus()));
            }
            builder.append("\n________________________________________________");
        }
        log.info(builder.toString());
    }

    private void createResultMap() {
        tests.forEach(t -> testMap.get(t.getCategory()).add(t));
    }
}
