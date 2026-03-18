package com.tpg;

import com.tpg.services.NovusLoggerService;
import com.tpg.services.NovusReportingService;
import com.tpg.utils.LocalCache;
import com.tpg.verification.NovusSoftAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.text.MessageFormat;

@SpringBootTest(classes = NovusApplication.class)
public class NovusApiTestBase extends AbstractTestNGSpringContextTests {

    @Autowired
    protected NovusReportingService reportingService;
    protected NovusLoggerService log;
    protected NovusSoftAssert softly;
    protected int stepCount = 0;

    @BeforeSuite(alwaysRun = true)
    @BeforeClass(alwaysRun = true)
    @Override
    protected void springTestContextPrepareTestInstance() throws Exception {
        super.springTestContextPrepareTestInstance();
    }

    @BeforeSuite(dependsOnMethods = "springTestContextPrepareTestInstance")
    @Parameters({"report-name"})
    public void initReport(@Optional("test-report") String report) {
        reportingService.init(report);
    }

    @BeforeClass(dependsOnMethods = "springTestContextPrepareTestInstance")
    public void baseBeforeClassSetup() {
        log = NovusLoggerService.init(this.getClass());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup(Method method) {
        stepCount = 0;
        softly = new NovusSoftAssert();
        reportingService.addTest(method);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethodSetup(ITestResult result) {
        reportingService.attachResult(result, null);
    }

    @AfterClass(alwaysRun = true)
    public void saveReport(ITestContext result) {
        for (ITestNGMethod allMethod : result.getSkippedTests().getAllMethods()) {
            reportingService.addTest(allMethod.getConstructorOrMethod().getMethod());
            reportingService.attachResult();
        }
        reportingService.saveReport();
    }

    @AfterSuite(alwaysRun = true)
    public void tearDown() {
        LocalCache.remove();
    }

    protected synchronized void step(String step, Object... obj) {
        var count = ++stepCount;
        reportingService.addStep(MessageFormat.format("[Test Step : #{0}] - {1}", count, step), obj);
        log.step(MessageFormat.format("[Test Step : #{0}] - {1}", count, step), obj);
    }
}
