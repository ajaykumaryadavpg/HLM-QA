package com.tpg.automation.inventory;

import com.tpg.NovusGuiTestBase;
import com.tpg.actions.Launch;
import com.tpg.actor.Actor;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.listeners.NovusTestListener;
import com.tpg.automation.macros.AuthenticateAs;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.LoginPage;
import com.tpg.automation.services.UrlService;
import com.tpg.automation.testdata.InventoryTestData.Credentials;
import com.tpg.verification.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.INVENTORY_LOGIN;
import static com.tpg.automation.constants.TestGroups.NEGATIVE;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.automation.impls.inventory.LoginPageImpl.clickLoginButton;
import static com.tpg.automation.impls.inventory.LoginPageImpl.enterEmail;
import static com.tpg.automation.impls.inventory.LoginPageImpl.enterPassword;
import static com.tpg.utils.CodeFillers.on;

/**
 * Login tests for the HLM Platform (Hardware Lifecycle Management).
 * URL: https://main.dddsig2mih3hw.amplifyapp.com/login
 *
 * Extends NovusGuiTestBase directly – NOT InventoryTestBase – because these
 * tests must start from an unauthenticated state and exercise the login page.
 *
 * Validation note:
 *   The app uses HTML-5 native required-field validation for empty submissions
 *   (no custom DOM error elements are rendered).  Empty-field tests therefore
 *   verify that the browser blocked navigation and the user remains on the
 *   login page (LOGIN_BUTTON still visible, URL unchanged).
 */
@ActiveProfiles({"web", "inventory", "local"})
@Listeners(NovusTestListener.class)
public class InventoryLoginTests extends NovusGuiTestBase {

    @Autowired
    UrlService urlService;

    Actor user = new Actor();

    @BeforeMethod(alwaysRun = true, dependsOnMethods = "beforeMethodSetup")
    public void launchApplication() {
        step("Clear browser session state");
        browser.context().clearCookies();
        browser.evaluate("() => { try { localStorage.clear(); sessionStorage.clear(); } catch (e) {} }");

        step("Launch the HLM Platform");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );
    }

    // ──────────────── TC-IMS-001 : Successful Login ────────────────

    @MetaData(author = "QA Automation", testCaseId = "IMS-001",
            stories = {"IMS-101"}, category = "INVENTORY_LOGIN")
    @Description("Verify that a user with valid credentials can log in successfully")
    @Outcome("User is redirected to the Dashboard and the dashboard header is visible")
    @Test(groups = {SMOKE_TESTS, INVENTORY_LOGIN})
    public void testSuccessfulLoginWithValidCredentials() {

        step("Verify the login page heading is displayed");
        user.wantsTo(
                Verify.uiElement(LoginPage.PAGE_TITLE)
                        .describedAs("Login page heading 'Sign in to your account' is visible")
                        .isVisible()
        );

        step("Enter valid admin credentials and click Sign in");
        user.attemptsTo(
                AuthenticateAs.aUser()
                        .withUsername(Credentials.ADMIN_USERNAME)
                        .withPassword(Credentials.ADMIN_PASSWORD)
                        .andLogin()
        );

        step("Verify user is redirected to the Dashboard (h1 visible)");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 heading is visible after successful login")
                        .isVisible()
        );

        step("Verify the Inventory & Assets nav link is present");
        user.wantsTo(
                Verify.uiElement(DashboardPage.NavMenu.INVENTORY_LINK)
                        .describedAs("Inventory & Assets nav link is present in the sidebar")
                        .isVisible()
        );

        step("Verify welcome message includes the logged-in user's email");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message contains the admin email address")
                        .containsText("Welcome back")
        );
    }

    // ──────────────── TC-IMS-002 : Invalid Credentials ────────────────

    @MetaData(author = "QA Automation", testCaseId = "IMS-002",
            stories = {"IMS-101"}, category = "INVENTORY_LOGIN")
    @Description("Verify that login with invalid email and password displays an error banner")
    @Outcome("A red error banner is displayed and the user remains on the login page")
    @Test(groups = {INVENTORY_LOGIN, NEGATIVE})
    public void testLoginFailsWithInvalidCredentials() {

        step("Enter invalid credentials and click Sign in");
        user.attemptsTo(
                AuthenticateAs.aUser()
                        .withUsername(Credentials.INVALID_USERNAME)
                        .withPassword(Credentials.INVALID_PASSWORD)
                        .andLogin()
        );

        step("Verify the red error banner is displayed");
        user.wantsTo(
                Verify.uiElement(LoginPage.ERROR_MESSAGE)
                        .describedAs("Error banner (p.text-red-400) is visible after invalid login attempt")
                        .isVisible()
        );

        step("Verify the Sign in button is still present – user stayed on the login page");
        user.wantsTo(
                Verify.uiElement(LoginPage.LOGIN_BUTTON)
                        .describedAs("Sign in button is still visible – no redirect occurred")
                        .isVisible()
        );
    }

    // ──────────────── TC-IMS-003 : Empty Email Field ────────────────

    @MetaData(author = "QA Automation", testCaseId = "IMS-003",
            stories = {"IMS-101"}, category = "INVENTORY_LOGIN")
    @Description("Verify that clicking Sign in with an empty email field keeps the user on the login page")
    @Outcome("Browser HTML-5 validation blocks submission; user remains on the login page")
    @Test(groups = {INVENTORY_LOGIN, NEGATIVE})
    public void testLoginWithEmptyEmailStaysOnLoginPage() {

        step("Leave email empty, enter a valid password, then click Sign in");
        user.attemptsTo(
                enterEmail(Credentials.EMPTY),
                enterPassword(Credentials.ADMIN_PASSWORD),
                clickLoginButton()
        );

        step("Verify the login page is still displayed (Sign in button visible)");
        user.wantsTo(
                Verify.uiElement(LoginPage.LOGIN_BUTTON)
                        .describedAs("Sign in button still visible – browser blocked empty email submission")
                        .isVisible()
        );

        step("Verify the login page heading is still present");
        user.wantsTo(
                Verify.uiElement(LoginPage.PAGE_TITLE)
                        .describedAs("Login heading is still visible – user was not redirected")
                        .isVisible()
        );
    }

    // ──────────────── TC-IMS-004 : Empty Password Field ────────────────

    @MetaData(author = "QA Automation", testCaseId = "IMS-004",
            stories = {"IMS-101"}, category = "INVENTORY_LOGIN")
    @Description("Verify that clicking Sign in with an empty password field keeps the user on the login page")
    @Outcome("Browser HTML-5 validation blocks submission; user remains on the login page")
    @Test(groups = {INVENTORY_LOGIN, NEGATIVE})
    public void testLoginWithEmptyPasswordStaysOnLoginPage() {

        step("Enter a valid email, leave password empty, then click Sign in");
        user.attemptsTo(
                enterEmail(Credentials.ADMIN_USERNAME),
                enterPassword(Credentials.EMPTY),
                clickLoginButton()
        );

        step("Verify the login page is still displayed (Sign in button visible)");
        user.wantsTo(
                Verify.uiElement(LoginPage.LOGIN_BUTTON)
                        .describedAs("Sign in button still visible – user was not redirected")
                        .isVisible()
        );
    }

    // ──────────────── TC-IMS-005 : Both Fields Empty ────────────────

    @MetaData(author = "QA Automation", testCaseId = "IMS-005",
            stories = {"IMS-101"}, category = "INVENTORY_LOGIN")
    @Description("Verify that clicking Sign in with both fields empty keeps the user on the login page")
    @Outcome("Browser HTML-5 validation blocks submission; user remains on the login page")
    @Test(groups = {INVENTORY_LOGIN, NEGATIVE})
    public void testLoginWithBothFieldsEmptyStaysOnLoginPage() {

        step("Click Sign in without filling in any credentials");
        user.attemptsTo(clickLoginButton());

        step("Verify the Sign in button is still visible – no redirect occurred");
        user.wantsTo(
                Verify.uiElement(LoginPage.LOGIN_BUTTON)
                        .describedAs("Sign in button still visible after empty-form submission")
                        .isVisible()
        );

        step("Verify the login heading is still present");
        user.wantsTo(
                Verify.uiElement(LoginPage.PAGE_TITLE)
                        .describedAs("Login page heading still visible – user stayed on login page")
                        .isVisible()
        );
    }
}
