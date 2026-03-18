package com.tpg.automation.base;

import com.tpg.NovusGuiTestBase;
import com.tpg.actions.Launch;
import com.tpg.actions.Waiting;
import com.tpg.actor.Actor;
import com.tpg.automation.listeners.NovusTestListener;
import com.tpg.automation.macros.AuthenticateAs;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.LoginPage;
import com.tpg.automation.services.UrlService;
import com.tpg.automation.testdata.InventoryTestData.Credentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;

import static com.tpg.utils.CodeFillers.on;

/**
 * Base class for all Inventory Management UI tests that require an authenticated session.
 *
 * <p>Lifecycle:
 * <ol>
 *   <li>{@code NovusGuiTestBase.baseBeforeClassSetup} – initialises logger.
 *   <li>{@code InventoryTestBase.loginToApplication} – launches the AUT and authenticates
 *       <b>once per test class</b>; all test methods share the same browser session.
 *   <li>{@code NovusGuiTestBase.beforeMethodSetup} – resets step counter, initialises
 *       softly, registers each test with the Extent report.
 * </ol>
 *
 * <p>Credentials default to the constants in {@link Credentials}; override
 * {@code inventory.admin.username} / {@code inventory.admin.password} in
 * {@code application-inventory.properties} to use environment-specific accounts.
 */
@ActiveProfiles({"web", "inventory", "local"})
@Listeners(NovusTestListener.class)
public abstract class InventoryTestBase extends NovusGuiTestBase {

    @Autowired
    protected UrlService urlService;

    @Value("${inventory.admin.username:" + Credentials.ADMIN_USERNAME + "}")
    private String adminUsername;

    @Value("${inventory.admin.password:" + Credentials.ADMIN_PASSWORD + "}")
    private String adminPassword;

    protected Actor user = new Actor();

    /**
     * Logs in once per test class. All test methods in the class share the
     * same authenticated browser session — no re-login between tests.
     *
     * <p>Clears cookies and storage first to prevent stale sessions from
     * previous runs causing the app to skip the login page silently.
     */
    @BeforeClass(alwaysRun = true, dependsOnMethods = "baseBeforeClassSetup")
    public void loginToApplication() {
        log.step("Clear cached session state before login");
        browser.context().clearCookies();
        browser.evaluate("() => { try { localStorage.clear(); sessionStorage.clear(); } catch (e) {} }");

        log.step("Launch Inventory Management application");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        log.step("Wait for login page to be ready");
        user.is(Waiting.on(LoginPage.EMAIL_FIELD).within(15));

        log.step("Authenticate as admin user");
        user.attemptsTo(
                AuthenticateAs.aUser()
                        .withUsername(adminUsername)
                        .withPassword(adminPassword)
                        .andLogin()
        );

        log.step("Wait for Dashboard to load after login");
        if (!user.is(Waiting.on(DashboardPage.DASHBOARD_HEADER).within(30))) {
            throw new IllegalStateException("Login failed — dashboard not visible after 30 s. " +
                    "Check credentials in application-inventory.properties or application availability.");
        }
    }
}
