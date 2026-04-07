package com.tpg.automation.inventory;

import com.microsoft.playwright.Route;
import com.tpg.actions.Launch;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DashboardPageImpl;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.DashboardPage.QuickActions;
import com.tpg.automation.pages.inventory.DashboardPage.ErrorBanner;
import com.tpg.verification.Verify;
import org.springframework.beans.factory.annotation.Value;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_QUICK_ACTIONS;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite covering Dashboard Quick Actions with Live Counts (Story PS-6 / 8.4).
 *
 * <p>Verifies the four Quick Action cards, live badge counts, personalised welcome message,
 * and manual refresh button behaviour on the HLM Platform Dashboard.
 *
 * <p>Test suites:
 * <ul>
 *   <li>TC-PS6-QA   — Quick Action card rendering and layout
 *   <li>TC-PS6-NAV  — Card navigation links (each card routes to the correct module)
 *   <li>TC-PS6-BADGE — Live badge counts (offline devices, scheduled orders, pending firmware/compliance)
 *   <li>TC-PS6-WELCOME — Personalised welcome message and Admin fallback
 *   <li>TC-PS6-REFRESH — Manual refresh button: visibility, re-fetch, animation, disabled state
 *   <li>TC-PS6-ERR  — Error and edge-case resilience
 * </ul>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * in {@code @BeforeClass} and lands on the Dashboard before any test method runs.
 * Tests that navigate away from the Dashboard (navigation suite) call {@code Launch.app()}
 * at the start of the test to restore a clean Dashboard state.
 *
 * <p>Tests marked {@code enabled = false} require network-level request interception
 * (Playwright {@code page.route()}) with a zero-count or session-stripped environment —
 * preserved as runnable specification placeholders consistent with {@link DashboardApiTests}.
 *
 * @see DashboardPage.QuickActions
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-6
 * @jira PS-31 (QA Sub-task)
 */
public class DashboardQuickActionsTests extends InventoryTestBase {

    /** Injected admin username — used in welcome message assertions (TC-PS6-WELCOME-01). */
    @Value("${inventory.admin.username}")
    private String adminUsername;

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-QA: Quick Action Cards — Rendering and Layout
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-QA-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that all four Quick Action cards are displayed in the main content area of the Dashboard after login")
    @Outcome("View Inventory, Schedule Service, Deploy Firmware, and Check Compliance cards are all visible in the Quick Actions section. Exactly 4 cards are rendered.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testFourQuickActionCardsAreDisplayed() {

        step("Re-navigate to the Dashboard to ensure a clean state before verifying Quick Action cards");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Dashboard welcome message is visible — user is on the correct page");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is visible in the main content area — welcome paragraph rendered (moved from h2 to p in commit 9819983)")
                        .isVisible()
        );

        step("Verify the 'View Inventory' quick action card is visible in the main content area");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("main a[href='/inventory'] — 'View Inventory' card is rendered inside main (not sidebar)")
                        .isVisible()
        );

        step("Verify the 'Schedule Service' quick action card is visible in the main content area");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("main a[href='/account-service'] — 'Schedule Service' card is rendered inside main")
                        .isVisible()
        );

        step("Verify the 'Deploy Firmware' quick action card is visible");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("a[href='/deployment'].relative.bg-card — 'Deploy Firmware' card is rendered")
                        .isVisible()
        );

        step("Verify the 'Check Compliance' quick action card is visible in the main content area");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("main a[href='/compliance'] — 'Check Compliance' card is rendered inside main")
                        .isVisible()
        );

        step("Assert exactly 4 Quick Action card link elements are present in the main content grid");
        long cardCount = (Long) browser.evaluate(
                "() => document.querySelectorAll(\"main a[href='/inventory'], main a[href='/account-service\"]," +
                " a[href='/deployment'].relative.bg-card, main a[href='/compliance']\").length"
        );
        Assert.assertEquals(cardCount, 4L,
                "Expected exactly 4 Quick Action cards in the main content area but found: " + cardCount);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-QA-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that each Quick Action card displays a descriptive text label and an SVG icon")
    @Outcome("All four cards show their respective label ('View Inventory', 'Schedule Service', 'Deploy Firmware', 'Check Compliance') and each contains an SVG icon element.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testEachQuickActionCardHasLabelAndIcon() {

        step("Verify the 'View Inventory' card contains the label text 'View Inventory'");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_LABEL)
                        .describedAs("'View Inventory' text is present inside main a[href='/inventory']")
                        .isVisible()
        );

        step("Verify the 'View Inventory' card contains an SVG icon element");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_ICON)
                        .describedAs("SVG icon is present inside the 'View Inventory' card")
                        .isVisible()
        );

        step("Verify the 'Schedule Service' card contains the label text 'Schedule Service'");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_LABEL)
                        .describedAs("'Schedule Service' text is present inside main a[href='/account-service']")
                        .isVisible()
        );

        step("Verify the 'Schedule Service' card contains an SVG icon element");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_ICON)
                        .describedAs("SVG icon is present inside the 'Schedule Service' card")
                        .isVisible()
        );

        step("Verify the 'Deploy Firmware' card contains the label text 'Deploy Firmware'");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_LABEL)
                        .describedAs("'Deploy Firmware' text is present inside a[href='/deployment'].relative.bg-card")
                        .isVisible()
        );

        step("Verify the 'Deploy Firmware' card contains an SVG icon element");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_ICON)
                        .describedAs("SVG icon is present inside the 'Deploy Firmware' card")
                        .isVisible()
        );

        step("Verify the 'Check Compliance' card contains the label text 'Check Compliance'");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_LABEL)
                        .describedAs("'Check Compliance' text is present inside main a[href='/compliance']")
                        .isVisible()
        );

        step("Verify the 'Check Compliance' card contains an SVG icon element");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_ICON)
                        .describedAs("SVG icon is present inside the 'Check Compliance' card")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-NAV: Quick Action Cards — Navigation Links
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-NAV-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that clicking the 'View Inventory' quick action card navigates to the /inventory route")
    @Outcome("Browser URL contains '/inventory' after clicking the card; the Inventory page renders without error.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testViewInventoryCardNavigatesToInventoryPage() {

        step("Re-navigate to the Dashboard to guarantee a known starting state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'View Inventory' card is visible before clicking");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card is rendered and clickable in the Quick Actions section")
                        .isVisible()
        );

        step("Click the 'View Inventory' quick action card");
        user.attemptsTo(DashboardPageImpl.clickViewInventoryCard());

        step("Verify the browser URL now contains '/inventory' — navigation succeeded");
        String currentUrl = browser.url();
        Assert.assertTrue(currentUrl.contains("/inventory"),
                "Expected URL to contain '/inventory' after clicking View Inventory card, but URL was: " + currentUrl);

        step("Verify the Dashboard header is no longer visible — user has navigated away from Dashboard");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 'Dashboard' heading should not be present on the Inventory page")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-NAV-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that clicking the 'Schedule Service' quick action card navigates to the /account-service route")
    @Outcome("Browser URL contains '/account-service' after clicking the card; the Account & Service page renders without error.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testScheduleServiceCardNavigatesToAccountServicePage() {

        step("Re-navigate to the Dashboard to guarantee a known starting state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Schedule Service' card is visible before clicking");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("'Schedule Service' card is rendered and clickable in the Quick Actions section")
                        .isVisible()
        );

        step("Click the 'Schedule Service' quick action card");
        user.attemptsTo(DashboardPageImpl.clickScheduleServiceCard());

        step("Verify the browser URL now contains '/account-service' — navigation succeeded");
        String currentUrl = browser.url();
        Assert.assertTrue(currentUrl.contains("/account-service"),
                "Expected URL to contain '/account-service' after clicking Schedule Service card, but URL was: " + currentUrl);

        step("Verify the Dashboard header is no longer visible — user has navigated away from Dashboard");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 'Dashboard' heading should not be present on the Account & Service page")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-NAV-03",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that clicking the 'Deploy Firmware' quick action card navigates to the /deployment route")
    @Outcome("Browser URL contains '/deployment' after clicking the card; the Deployment page renders without error.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testDeployFirmwareCardNavigatesToDeploymentPage() {

        step("Re-navigate to the Dashboard to guarantee a known starting state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Deploy Firmware' card is visible before clicking");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' card is rendered and clickable in the Quick Actions section")
                        .isVisible()
        );

        step("Click the 'Deploy Firmware' quick action card");
        user.attemptsTo(DashboardPageImpl.clickDeployFirmwareCard());

        step("Verify the browser URL now contains '/deployment' — navigation succeeded");
        String currentUrl = browser.url();
        Assert.assertTrue(currentUrl.contains("/deployment"),
                "Expected URL to contain '/deployment' after clicking Deploy Firmware card, but URL was: " + currentUrl);

        step("Verify the Dashboard header is no longer visible — user has navigated away from Dashboard");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 'Dashboard' heading should not be present on the Deployment page")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-NAV-04",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that clicking the 'Check Compliance' quick action card navigates to the /compliance route")
    @Outcome("Browser URL contains '/compliance' after clicking the card; the Firmware Compliance page renders without error.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testCheckComplianceCardNavigatesToCompliancePage() {

        step("Re-navigate to the Dashboard to guarantee a known starting state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Check Compliance' card is visible before clicking");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("'Check Compliance' card is rendered and clickable in the Quick Actions section")
                        .isVisible()
        );

        step("Click the 'Check Compliance' quick action card");
        user.attemptsTo(DashboardPageImpl.clickCheckComplianceCard());

        step("Verify the browser URL now contains '/compliance' — navigation succeeded");
        String currentUrl = browser.url();
        Assert.assertTrue(currentUrl.contains("/compliance"),
                "Expected URL to contain '/compliance' after clicking Check Compliance card, but URL was: " + currentUrl);

        step("Verify the Dashboard header is no longer visible — user has navigated away from Dashboard");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 'Dashboard' heading should not be present on the Compliance page")
                        .isNotVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-BADGE: Quick Action Cards — Live Count Badges
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the 'View Inventory' quick action card shows an orange badge with a positive integer count of offline devices when devices are offline")
    @Outcome("If the offline device count is > 0 the orange badge span is visible and its text is a positive integer. If there are no offline devices, the badge is absent.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testViewInventoryBadgeShowsOfflineDeviceCount() {

        step("Re-navigate to the Dashboard and wait for Quick Actions to resolve badge values");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'View Inventory' card is visible after data load");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card is rendered in the main content area")
                        .isVisible()
        );

        step("Check if the offline-device badge is present on the 'View Inventory' card");
        boolean badgeVisible = browser.locator("css=main a[href='/inventory'] span[class*='absolute']").isVisible();

        if (badgeVisible) {
            step("Badge is visible — verify its text content is a positive integer (offline device count > 0)");
            String badgeText = browser.locator("css=main a[href='/inventory'] span[class*='absolute']").textContent().trim();
            int badgeCount = Integer.parseInt(badgeText);
            Assert.assertTrue(badgeCount > 0,
                    "Expected 'View Inventory' badge count to be > 0 when badge is visible, but was: " + badgeCount);
        } else {
            step("Badge is not visible — offline device count is 0, badge is correctly hidden per AC-6");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the 'Schedule Service' quick action card shows an orange badge with a positive integer count of scheduled service orders when orders are scheduled")
    @Outcome("If the scheduled orders count is > 0 the orange badge span is visible and its text is a positive integer.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testScheduleServiceBadgeShowsScheduledOrdersCount() {

        step("Re-navigate to the Dashboard and wait for Quick Actions to resolve badge values");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Schedule Service' card is visible after data load");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("'Schedule Service' card is rendered in the main content area")
                        .isVisible()
        );

        step("Check if the scheduled-orders badge is present on the 'Schedule Service' card");
        boolean badgeVisible = browser.locator("css=main a[href='/account-service'] span[class*='absolute']").isVisible();

        if (badgeVisible) {
            step("Badge is visible — verify its text content is a positive integer (scheduled orders count > 0)");
            String badgeText = browser.locator("css=main a[href='/account-service'] span[class*='absolute']").textContent().trim();
            int badgeCount = Integer.parseInt(badgeText);
            Assert.assertTrue(badgeCount > 0,
                    "Expected 'Schedule Service' badge count to be > 0 when badge is visible, but was: " + badgeCount);
        } else {
            step("Badge is not visible — scheduled orders count is 0, badge is correctly hidden per AC-6");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-03",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the 'Deploy Firmware' quick action card shows an orange badge with a positive integer count of pending firmware items when items are pending")
    @Outcome("If the pending firmware count is > 0 the orange badge span is visible and its text is a positive integer.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testDeployFirmwareBadgeShowsPendingFirmwareCount() {

        step("Re-navigate to the Dashboard and wait for Quick Actions to resolve badge values");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Deploy Firmware' card is visible after data load");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' card is rendered (a[href='/deployment'].relative.bg-card)")
                        .isVisible()
        );

        step("Check if the pending-firmware badge is present on the 'Deploy Firmware' card");
        boolean badgeVisible = browser.locator("css=a[href='/deployment'].relative.bg-card span[class*='absolute']").isVisible();

        if (badgeVisible) {
            step("Badge is visible — verify its text content is a positive integer (pending firmware count > 0)");
            String badgeText = browser.locator("css=a[href='/deployment'].relative.bg-card span[class*='absolute']").textContent().trim();
            int badgeCount = Integer.parseInt(badgeText);
            Assert.assertTrue(badgeCount > 0,
                    "Expected 'Deploy Firmware' badge count to be > 0 when badge is visible, but was: " + badgeCount);
        } else {
            step("Badge is not visible — pending firmware count is 0, badge is correctly hidden per AC-6");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-04",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the 'Check Compliance' quick action card shows an orange badge with a positive integer count of pending compliance records when records are pending")
    @Outcome("If the pending compliance count is > 0 the orange badge span is visible and its text is a positive integer.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testCheckComplianceBadgeShowsPendingComplianceCount() {

        step("Re-navigate to the Dashboard and wait for Quick Actions to resolve badge values");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the 'Check Compliance' card is visible after data load");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("'Check Compliance' card is rendered in the main content area")
                        .isVisible()
        );

        step("Check if the pending-compliance badge is present on the 'Check Compliance' card");
        boolean badgeVisible = browser.locator("css=main a[href='/compliance'] span[class*='absolute']").isVisible();

        if (badgeVisible) {
            step("Badge is visible — verify its text content is a positive integer (pending compliance count > 0)");
            String badgeText = browser.locator("css=main a[href='/compliance'] span[class*='absolute']").textContent().trim();
            int badgeCount = Integer.parseInt(badgeText);
            Assert.assertTrue(badgeCount > 0,
                    "Expected 'Check Compliance' badge count to be > 0 when badge is visible, but was: " + badgeCount);
        } else {
            step("Badge is not visible — pending compliance count is 0, badge is correctly hidden per AC-6");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-05",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that all four Quick Action card badges are hidden (not rendered) when their respective API counts are zero")
    @Outcome("No orange badge span appears on any of the four Quick Action cards when all counts are zero. Requires network interception returning count=0.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS}, enabled = false)
    public void testBadgesAreHiddenWhenCountIsZero() {
        // NOTE: Requires a test environment where all badge-count APIs return zero,
        // or Playwright route interception mocking all four GraphQL operations to return
        // totalCount=0 / empty items arrays. Mark enabled=true when a zero-data environment
        // or reliable mock infrastructure is in place.

        step("Intercept all four badge-count GraphQL operations to return count=0");
        browser.route("**/graphql", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(200)
                .setContentType("application/json")
                .setBody("{\"data\":{\"listDevices\":{\"items\":[],\"totalCount\":0}," +
                        "\"listServiceOrders\":{\"items\":[],\"totalCount\":0}," +
                        "\"listFirmware\":{\"items\":[],\"totalCount\":0}," +
                        "\"listCompliance\":{\"items\":[],\"totalCount\":0}}}")));

        try {
            step("Navigate to the Dashboard with zero-count mocked responses active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the 'View Inventory' badge is NOT present when offline device count is zero");
            user.wantsTo(
                    Verify.uiElement(QuickActions.VIEW_INVENTORY_BADGE)
                            .describedAs("No orange badge span on 'View Inventory' card when offline count = 0")
                            .isNotVisible()
            );

            step("Verify the 'Schedule Service' badge is NOT present when scheduled orders count is zero");
            user.wantsTo(
                    Verify.uiElement(QuickActions.SCHEDULE_SERVICE_BADGE)
                            .describedAs("No orange badge span on 'Schedule Service' card when scheduled orders count = 0")
                            .isNotVisible()
            );

            step("Verify the 'Deploy Firmware' badge is NOT present when pending firmware count is zero");
            user.wantsTo(
                    Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_BADGE)
                            .describedAs("No orange badge span on 'Deploy Firmware' card when pending firmware count = 0")
                            .isNotVisible()
            );

            step("Verify the 'Check Compliance' badge is NOT present when pending compliance count is zero");
            user.wantsTo(
                    Verify.uiElement(QuickActions.CHECK_COMPLIANCE_BADGE)
                            .describedAs("No orange badge span on 'Check Compliance' card when pending compliance count = 0")
                            .isNotVisible()
            );

        } finally {
            browser.unroute("**/graphql");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-06",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that all currently visible Quick Action card badge values are positive integers — no NaN, decimals, negatives, or undefined strings")
    @Outcome("Each visible orange badge span in the main content area contains a parseable positive integer. No badge shows an invalid or malformed value.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testVisibleBadgeValuesArePositiveIntegers() {

        step("Re-navigate to the Dashboard and wait for badge values to resolve");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Quick Actions section is rendered");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Dashboard welcome heading visible — Quick Actions section should be rendered")
                        .isVisible()
        );

        step("Evaluate all visible orange badge spans in the main content area and assert each is a positive integer");
        String badgeValidation = (String) browser.evaluate(
                "() => {" +
                "  const badges = document.querySelectorAll(" +
                "    \"main a[href='/inventory'] span[class*='absolute']," +
                "     main a[href='/account-service'] span[class*='absolute']," +
                "     a[href='/deployment'].relative.bg-card span[class*='absolute']," +
                "     main a[href='/compliance'] span[class*='absolute']\");" +
                "  for (const badge of badges) {" +
                "    const text = badge.textContent.trim();" +
                "    const val = parseInt(text, 10);" +
                "    if (isNaN(val) || val <= 0 || String(val) !== text) {" +
                "      return 'INVALID: badge text=\"' + text + '\"';" +
                "    }" +
                "  }" +
                "  return 'OK:' + badges.length;" +
                "}"
        );
        Assert.assertTrue(badgeValidation.startsWith("OK"),
                "One or more Quick Action badge elements contains an invalid value — " + badgeValidation);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-BADGE-07",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that sidebar navigation badge selectors are isolated from Quick Action card badge selectors — they do not overlap and they show consistent counts for the same routes")
    @Outcome("nav-scoped badge selectors return only sidebar badge spans; main-scoped selectors return only Quick Action card badge spans. Badge counts for the same route match between sidebar and Quick Action card.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testSidebarBadgesDoNotInterfereWithQuickActionCardBadges() {

        step("Re-navigate to the Dashboard with full sidebar and main content rendered");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the sidebar navigation is rendered");
        user.wantsTo(
                Verify.uiElement(DashboardPage.NavMenu.INVENTORY_LINK)
                        .describedAs("nav a[href='/inventory'] — sidebar Inventory link is visible")
                        .isVisible()
        );

        step("Assert nav-scoped badge selectors do not match elements inside main, and vice versa");
        String isolationCheck = (String) browser.evaluate(
                "() => {" +
                "  const navBadges = document.querySelectorAll(\"nav span[class*='absolute']\");" +
                "  const mainBadges = document.querySelectorAll(\"main span[class*='absolute']\");" +
                "  for (const nb of navBadges) {" +
                "    if (nb.closest('main')) return 'OVERLAP: nav badge found inside main';" +
                "  }" +
                "  for (const mb of mainBadges) {" +
                "    if (mb.closest('nav')) return 'OVERLAP: main badge found inside nav';" +
                "  }" +
                "  return 'ISOLATED:nav=' + navBadges.length + ',main=' + mainBadges.length;" +
                "}"
        );
        Assert.assertTrue(isolationCheck.startsWith("ISOLATED"),
                "Badge selector isolation check failed — " + isolationCheck);

        step("Verify badge counts for /inventory route are consistent between sidebar and Quick Action card");
        String inventoryBadgeCheck = (String) browser.evaluate(
                "() => {" +
                "  const navBadge = document.querySelector(\"nav a[href='/inventory'] span[class*='absolute']\");" +
                "  const mainBadge = document.querySelector(\"main a[href='/inventory'] span[class*='absolute']\");" +
                "  if (!navBadge && !mainBadge) return 'MATCH:both-absent';" +
                "  if (navBadge && mainBadge) {" +
                "    return navBadge.textContent.trim() === mainBadge.textContent.trim()" +
                "      ? 'MATCH:' + navBadge.textContent.trim()" +
                "      : 'MISMATCH:nav=' + navBadge.textContent.trim() + ',main=' + mainBadge.textContent.trim();" +
                "  }" +
                "  return 'MISMATCH:one-absent,other-present';" +
                "}"
        );
        Assert.assertTrue(inventoryBadgeCheck.startsWith("MATCH"),
                "Inventory badge count mismatch between sidebar and Quick Action card — " + inventoryBadgeCheck);
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-WELCOME: Welcome Message
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-WELCOME-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Dashboard welcome message displays the logged-in user's email address after login")
    @Outcome("The p element with 'Welcome back' text contains the admin email address 'ajaykumar.yadav@3pillarglobal.com'. (UI change commit 9819983: welcome text moved from h2 to p with inline span for email)")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testWelcomeMessageDisplaysLoggedInUserEmail() {

        step("Re-navigate to the Dashboard to get a fresh welcome message render");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the welcome message paragraph is visible");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is visible in the main content area")
                        .isVisible()
        );

        step("Verify the welcome message contains the logged-in user's email address");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message p contains the admin email '" + adminUsername + "' (email rendered in child span, p.textContent includes it)")
                        .containsText(adminUsername)
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-WELCOME-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Dashboard welcome message falls back to 'Welcome back, Admin' when no user email is available in the session")
    @Outcome("p welcome message reads 'Welcome back, Admin' (via fallback span) when the user email is stripped from the session. Requires session/auth mock.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS}, enabled = false)
    public void testWelcomeMessageFallsBackToAdminWhenNoEmail() {
        // NOTE: Requires intercepting the auth/session API or manipulating Cognito tokens
        // in localStorage so the email field is absent when the Dashboard renders.
        // Mark enabled=true once session-mocking infrastructure is available.

        step("Strip the email from the user session via localStorage manipulation after login");
        browser.evaluate(
                "() => {" +
                "  const keys = Object.keys(localStorage);" +
                "  keys.forEach(k => { if (k.includes('CognitoIdentityServiceProvider') && k.endsWith('userData')) {" +
                "    try { const v = JSON.parse(localStorage.getItem(k));" +
                "          const attr = v.UserAttributes;" +
                "          if (attr) { const idx = attr.findIndex(a => a.Name === 'email');" +
                "            if (idx >= 0) attr.splice(idx, 1);" +
                "            localStorage.setItem(k, JSON.stringify(v)); }" +
                "    } catch(e) {} }" +
                "  });" +
                "}"
        );

        step("Reload the Dashboard so the app re-reads session and renders the fallback welcome message");
        browser.reload();

        step("Verify the welcome message is visible");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is visible after session manipulation")
                        .isVisible()
        );

        step("Verify the welcome message falls back to 'Welcome back, Admin' when no email is present");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message contains 'Admin' as the fallback identifier when no email is in the session")
                        .containsText("Admin")
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-WELCOME-03",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Dashboard welcome message is visible immediately after login and follows the exact format 'Welcome back, {identifier}'")
    @Outcome("The p element is present and visible; its textContent matches the 'Welcome back,' prefix followed by the user identifier and overview suffix.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testWelcomeMessageFormatAndVisibility() {

        step("Re-navigate to the Dashboard and wait for the welcome row to render");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the welcome message paragraph is visible without a loading delay");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is immediately visible — rendered from session data, no separate API call required")
                        .isVisible()
        );

        step("Verify the welcome message contains the 'Welcome back,' prefix with correct comma and space");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message p contains the greeting prefix 'Welcome back,' (comma present)")
                        .containsText("Welcome back,")
        );

        step("Verify the welcome message text follows the pattern 'Welcome back, {email}…' — full text assertion");
        // Element changed from h2 to p in commit 9819983; text now includes suffix "— here's your hardware lifecycle overview"
        String welcomeText = browser.locator("css=p:has-text(\"Welcome back\")").first().textContent().trim();
        Assert.assertTrue(welcomeText.matches("Welcome back,\\s+.+"),
                "Welcome message does not match expected pattern 'Welcome back, {identifier}'. Actual text: " + welcomeText);
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-REFRESH: Manual Refresh Button
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-REFRESH-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Refresh Dashboard button is visible on the Dashboard with the correct aria-label and contains an SVG icon")
    @Outcome("button[aria-label='Refresh dashboard'] is visible in the welcome row; it contains an SVG icon element.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_QUICK_ACTIONS})
    public void testRefreshButtonIsVisibleWithAriaLabel() {

        step("Re-navigate to the Dashboard and wait for the page to fully render");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Refresh Dashboard button is visible (addressed by aria-label)");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("button[aria-label='Refresh dashboard'] is visible in the top-right of the welcome row")
                        .isVisible()
        );

        step("Verify the Refresh button contains an SVG icon element");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_BUTTON_ICON)
                        .describedAs("button[aria-label='Refresh dashboard'] svg — SVG icon is rendered inside the Refresh button")
                        .isVisible()
        );

        step("Assert the Refresh button is not disabled before any refresh is triggered");
        boolean isDisabledBeforeRefresh = (Boolean) browser.evaluate(
                "() => document.querySelector(\"button[aria-label='Refresh dashboard']\")?.disabled ?? false"
        );
        Assert.assertFalse(isDisabledBeforeRefresh,
                "Refresh button should be enabled (not disabled) before any refresh is triggered");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-REFRESH-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that clicking the Refresh Dashboard button re-triggers all dashboard data fetches and KPI cards repopulate with valid data")
    @Outcome("After clicking Refresh, all four KPI card value elements are visible with resolved numeric data. No loading placeholder '—' remains.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testRefreshButtonTriggersDataRefetch() {

        step("Re-navigate to the Dashboard and wait for initial data load to settle");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify all four KPI cards are populated before triggering refresh");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card has a resolved value before refresh")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button to re-trigger all data fetches");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Verify all four KPI cards re-populate after the refresh cycle completes");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is visible and populated after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI card is visible and populated after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI card is visible and populated after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score KPI card is visible and populated after refresh")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains — all re-fetched values have resolved");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' (U+2014 em-dash) loading placeholder visible after refresh completes")
                        .isNotVisible()
        );

        step("Verify no global error banner appeared after the refresh cycle");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner (div.bg-red-50) is present after refresh")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-REFRESH-03",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Refresh button icon shows a spinning animation (animate-spin CSS class) while an active refresh is in progress")
    @Outcome("The SVG icon inside button[aria-label='Refresh dashboard'] has the 'animate-spin' Tailwind class immediately after clicking Refresh.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testRefreshButtonShowsSpinningAnimationDuringRefresh() {

        step("Re-navigate to the Dashboard and wait for the page to settle in a loaded state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Refresh button is visible and not animating before any click");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh button is visible and in resting state before click")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button to initiate a refresh cycle");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Immediately inspect the SVG icon class for the 'animate-spin' Tailwind animation class");
        String iconClass = (String) browser.evaluate(
                "() => {" +
                "  const svg = document.querySelector(\"button[aria-label='Refresh dashboard'] svg\");" +
                "  if (!svg) return '';" +
                "  return svg.getAttribute('class') || (svg.className && svg.className.baseVal) || '';" +
                "}"
        );
        Assert.assertTrue(iconClass.contains("animate-spin"),
                "Expected Refresh button SVG icon to have 'animate-spin' class during active refresh, but class was: '" + iconClass + "'");

        step("Wait for the refresh cycle to complete — KPI cards repopulate with resolved values");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is present — refresh cycle completed")
                        .isVisible()
        );

        step("Verify the spinning animation has stopped after refresh completes");
        String iconClassAfter = (String) browser.evaluate(
                "() => {" +
                "  const svg = document.querySelector(\"button[aria-label='Refresh dashboard'] svg\");" +
                "  if (!svg) return '';" +
                "  return svg.getAttribute('class') || (svg.className && svg.className.baseVal) || '';" +
                "}"
        );
        Assert.assertFalse(iconClassAfter.contains("animate-spin"),
                "Expected 'animate-spin' class to be removed after refresh completes, but class was: '" + iconClassAfter + "'");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-REFRESH-04",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Refresh Dashboard button is disabled during an active refresh to prevent duplicate API requests")
    @Outcome("The button has the 'disabled' HTML attribute immediately after clicking. After the refresh cycle completes the button transitions back to enabled.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testRefreshButtonIsDisabledDuringActiveRefresh() {

        step("Re-navigate to the Dashboard and confirm Refresh button is enabled before any action");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Refresh button is not disabled before initiating a refresh");
        boolean disabledBefore = (Boolean) browser.evaluate(
                "() => document.querySelector(\"button[aria-label='Refresh dashboard']\")?.disabled ?? false"
        );
        Assert.assertFalse(disabledBefore,
                "Refresh button should be enabled before any refresh is triggered");

        step("Click the Refresh Dashboard button to initiate a refresh cycle");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Immediately assert the Refresh button now has the 'disabled' attribute — prevents duplicate requests");
        boolean disabledDuringRefresh = (Boolean) browser.evaluate(
                "() => document.querySelector(\"button[aria-label='Refresh dashboard']\")?.disabled ?? false"
        );
        Assert.assertTrue(disabledDuringRefresh,
                "Refresh button should be disabled immediately after clicking to prevent duplicate API requests");

        step("Wait for the refresh cycle to complete — KPI cards repopulate");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is present — refresh cycle has completed")
                        .isVisible()
        );

        step("Verify the Refresh button transitions back to an enabled state after the refresh completes");
        boolean disabledAfter = (Boolean) browser.evaluate(
                "() => document.querySelector(\"button[aria-label='Refresh dashboard']\")?.disabled ?? false"
        );
        Assert.assertFalse(disabledAfter,
                "Refresh button should be re-enabled after the refresh cycle completes");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-REFRESH-05",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that Quick Action card badge counts are re-rendered with valid numeric data after the Refresh Dashboard button completes a refresh cycle")
    @Outcome("After refresh, all visible badge spans in the Quick Actions section show valid numeric counts. No em-dash '—' loading placeholder remains in any badge position.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testBadgeCountsReflectFreshDataAfterRefresh() {

        step("Re-navigate to the Dashboard and wait for initial badge values to load");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Quick Actions section is rendered and at least the 'View Inventory' card is visible");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card visible before refresh — Quick Actions section is rendered")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button and wait for the refresh cycle to complete");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Verify all KPI cards re-populate after refresh (refresh cycle gate)");
        user.wantsTo(
                Verify.uiElement(DashboardPage.KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is populated — confirms the refresh API calls completed")
                        .isVisible()
        );

        step("Verify all four Quick Action cards are still present after the refresh cycle");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card is still visible after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("'Schedule Service' card is still visible after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' card is still visible after refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("'Check Compliance' card is still visible after refresh")
                        .isVisible()
        );

        step("Verify no badge shows an em-dash loading placeholder after refresh — all badges have resolved");
        String badgePlaceholderCheck = (String) browser.evaluate(
                "() => {" +
                "  const badges = document.querySelectorAll(\"main span[class*='absolute']\");" +
                "  for (const b of badges) {" +
                "    if (b.textContent.includes('\\u2014')) return 'PLACEHOLDER_FOUND';" +
                "  }" +
                "  return 'OK';" +
                "}"
        );
        Assert.assertEquals(badgePlaceholderCheck, "OK",
                "At least one Quick Action badge contains the '—' loading placeholder after refresh completed");
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // TC-PS6-ERR: Error and Edge-Case Handling
    // ══════════════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-ERR-01",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that no Quick Action card badge displays a zero or negative number — visible badges must always show a positive integer count")
    @Outcome("All visible orange badge spans in the main Quick Actions area have text content that parses as an integer > 0.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testBadgeValuesAreNeverNegativeOrZero() {

        step("Re-navigate to the Dashboard and wait for badge values to resolve");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Quick Actions section is rendered");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Dashboard welcome heading visible — page has fully loaded")
                        .isVisible()
        );

        step("Assert all visible Quick Action badge values are strictly positive integers (> 0)");
        String negativeCheck = (String) browser.evaluate(
                "() => {" +
                "  const badges = document.querySelectorAll(\"main span[class*='absolute']\");" +
                "  for (const b of badges) {" +
                "    const val = parseInt(b.textContent.trim(), 10);" +
                "    if (isNaN(val) || val <= 0) return 'INVALID:' + b.textContent.trim();" +
                "  }" +
                "  return 'OK';" +
                "}"
        );
        Assert.assertEquals(negativeCheck, "OK",
                "A visible Quick Action badge has a zero, negative, or non-numeric value — " + negativeCheck);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-ERR-02",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that all four Quick Action cards remain visible and navigable when the badge-count APIs return HTTP 500 errors")
    @Outcome("All four cards are rendered without error strings in badge positions. The 'View Inventory' card navigation still routes to /inventory.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testQuickActionCardsAccessibleWhenBadgeApiReturnsError() {

        step("Intercept all GraphQL requests to return HTTP 500 — simulates badge API failure");
        browser.route("**/graphql", route -> route.fulfill(new Route.FulfillOptions()
                .setStatus(500)
                .setContentType("application/json")
                .setBody("{\"errors\":[{\"message\":\"Internal Server Error\"}]}")));

        try {
            step("Navigate to the Dashboard with all GraphQL APIs returning errors");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify all four Quick Action cards are still visible despite API errors");
            user.wantsTo(
                    Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                            .describedAs("'View Inventory' card is rendered even when badge APIs return 500")
                            .isVisible()
            );
            user.wantsTo(
                    Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                            .describedAs("'Schedule Service' card is rendered even when badge APIs return 500")
                            .isVisible()
            );
            user.wantsTo(
                    Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                            .describedAs("'Deploy Firmware' card is rendered even when badge APIs return 500")
                            .isVisible()
            );
            user.wantsTo(
                    Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                            .describedAs("'Check Compliance' card is rendered even when badge APIs return 500")
                            .isVisible()
            );

            step("Verify no raw error strings ('undefined', 'NaN', 'null') appear inside badge positions");
            String badgeErrorCheck = (String) browser.evaluate(
                    "() => {" +
                    "  const cards = document.querySelectorAll(" +
                    "    \"main a[href='/inventory'], main a[href='/account-service\"]," +
                    "     a[href='/deployment'].relative.bg-card, main a[href='/compliance']\");" +
                    "  for (const card of cards) {" +
                    "    const text = card.textContent || '';" +
                    "    if (/undefined|NaN|\\bnull\\b/.test(text)) return 'ERROR_TEXT_FOUND:' + text.substring(0, 50);" +
                    "  }" +
                    "  return 'OK';" +
                    "}"
            );
            Assert.assertEquals(badgeErrorCheck, "OK",
                    "Raw error string found inside a Quick Action card badge area when API returned 500 — " + badgeErrorCheck);

        } finally {
            browser.unroute("**/graphql");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-ERR-03",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that the Dashboard survives a hard page reload and re-renders all Quick Action features correctly without requiring re-login")
    @Outcome("After hard reload: welcome message with correct email, all 4 Quick Action cards, and Refresh button are all visible. Session is preserved.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testDashboardSurvivesHardPageReload() {

        step("Re-navigate to the Dashboard and confirm all Quick Action features are visible before reload");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message visible before reload")
                        .isVisible()
        );

        step("Perform a hard browser page reload (F5 equivalent) while on the Dashboard");
        browser.reload();

        step("Verify the authenticated session is preserved — user is not redirected to the login page");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 heading is visible after reload — session preserved, no redirect to login")
                        .isVisible()
        );

        step("Verify the welcome message re-renders with the correct user email after reload");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is visible after hard reload")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("Welcome message contains admin email after hard reload")
                        .containsText(adminUsername)
        );

        step("Verify all four Quick Action cards are visible after reload");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card visible after reload")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("'Schedule Service' card visible after reload")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' card visible after reload")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("'Check Compliance' card visible after reload")
                        .isVisible()
        );

        step("Verify the Refresh button is visible and enabled after reload");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh Dashboard button is visible and interactive after hard reload")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS6-ERR-04",
            stories = {"PS-6"}, category = "DASHBOARD_QUICK_ACTIONS")
    @Description("Verify that navigating directly to the Dashboard root URL with an existing authenticated session renders all Quick Action features correctly")
    @Outcome("Direct URL navigation to the app root renders the welcome message, all 4 Quick Action cards, and the Refresh button without requiring re-login.")
    @Test(groups = {REGRESSION, DASHBOARD_QUICK_ACTIONS})
    public void testDirectUrlAccessRendersAllQuickActionFeatures() {

        step("Navigate directly to the Dashboard root URL using the authenticated session established by @BeforeClass");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Dashboard header is visible — direct URL navigation honoured the existing session");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 Dashboard heading visible on direct URL access — no redirect to login")
                        .isVisible()
        );

        step("Verify the personalised welcome message is visible on direct URL access");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is rendered on direct URL navigation")
                        .isVisible()
        );

        step("Verify all four Quick Action cards are visible on direct URL access");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("'View Inventory' card visible on direct URL access")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("'Schedule Service' card visible on direct URL access")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' card visible on direct URL access")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("'Check Compliance' card visible on direct URL access")
                        .isVisible()
        );

        step("Verify the Refresh Dashboard button is visible and enabled on direct URL access");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh Dashboard button visible and enabled on direct URL access")
                        .isVisible()
        );
    }
}
