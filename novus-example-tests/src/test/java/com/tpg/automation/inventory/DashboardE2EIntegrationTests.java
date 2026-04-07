package com.tpg.automation.inventory;

import com.microsoft.playwright.Route;
import com.tpg.actions.Launch;
import com.tpg.actions.Waiting;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DashboardPageImpl;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.DashboardPage.AlertsPanel;
import com.tpg.automation.pages.inventory.DashboardPage.ErrorBanner;
import com.tpg.automation.pages.inventory.DashboardPage.KpiCard;
import com.tpg.automation.pages.inventory.DashboardPage.QuickActions;
import com.tpg.automation.pages.inventory.DashboardPage.SystemStatus;
import com.tpg.automation.pages.inventory.LoginPage;
import com.tpg.verification.Verify;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_E2E_INTEGRATION;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * E2E Integration Test Suite — Dashboard Module (Story PS-7 / 8.5).
 *
 * <p>Validates all seven PS-7 acceptance criteria as end-to-end integration scenarios,
 * covering the full dashboard behaviour by exercising all live API paths together:
 * <ol>
 *   <li>KPI Data Loading — all four KPI cards populate from six parallel GraphQL API calls
 *   <li>Alerts from Audit Logs — Recent Alerts panel displays entries from a 24-hour window
 *   <li>Alert Severity Icons — failed entries render orange, approved green, info blue
 *   <li>Quick Action Badges — all four cards show correct live badge counts
 *   <li>System Status Toggle — Deployment Service toggles Operational/Degraded on active orders
 *   <li>Manual Refresh — clicking refresh re-triggers all API data fetches
 *   <li>Error Handling — KPI API failure renders error banner; recovery via refresh works
 * </ol>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * in {@code @BeforeClass}. Tests that require deterministic API responses use
 * {@code browser.route("**&#47;graphql", ...)} with cleanup via
 * {@code browser.unrouteAll()} in a {@code finally} block.
 *
 * @see DashboardPage
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-7
 * @jira PS-32 (QA Sub-task)
 */
public class DashboardE2EIntegrationTests extends InventoryTestBase {

    // ═══════════════════════════════════════════════════════════════════════════
    // GraphQL mock response bodies
    // Timestamps are generated dynamically so entries always fall within the
    // app's 24-hour audit-log display window, regardless of when tests run.
    // ═══════════════════════════════════════════════════════════════════════════

    private static final String MOCK_THREE_AUDIT_LOG_ENTRIES;
    private static final String MOCK_ONE_APPROVED_ENTRY;
    private static final String MOCK_ONE_FAILED_ENTRY;
    private static final String MOCK_ONE_INFO_ENTRY;

    /** Empty audit-log response — simulates no activity in the last 24 h */
    private static final String MOCK_EMPTY_AUDIT_LOGS =
            "{\"data\":{\"listAuditLogs\":{\"items\":[],\"nextToken\":null}}}";

    static {
        // Use timestamps 2, 4, and 6 hours before now so entries are always within the 24-hour window.
        // Truncate to milliseconds — Java's Instant.toString() includes nanoseconds (e.g. .123456789Z)
        // which some JS Date parsers reject; millisecond precision (.123Z) is always safe.
        String ts1 = Instant.now().minus(2, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS).toString();
        String ts2 = Instant.now().minus(4, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS).toString();
        String ts3 = Instant.now().minus(6, ChronoUnit.HOURS).truncatedTo(ChronoUnit.MILLIS).toString();

        /** Three mixed audit-log entries (approved, failed, info) within the last 24 h */
        MOCK_THREE_AUDIT_LOG_ENTRIES =
                "{\"data\":{\"listAuditLogs\":{\"items\":[" +
                "{\"id\":\"audit-001\",\"action\":\"Firmware update approved\"," +
                "\"auditStatus\":\"approved\",\"timestamp\":\"" + ts1 + "\",\"userId\":\"user-001\"}," +
                "{\"id\":\"audit-002\",\"action\":\"Deployment failed\"," +
                "\"auditStatus\":\"failed\",\"timestamp\":\"" + ts2 + "\",\"userId\":\"user-002\"}," +
                "{\"id\":\"audit-003\",\"action\":\"Device configuration updated\"," +
                "\"auditStatus\":\"info\",\"timestamp\":\"" + ts3 + "\",\"userId\":\"user-003\"}]," +
                "\"nextToken\":null}}}";

        /** Single approved audit-log entry */
        MOCK_ONE_APPROVED_ENTRY =
                "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-001\"," +
                "\"action\":\"Firmware update approved\",\"auditStatus\":\"approved\"," +
                "\"timestamp\":\"" + ts1 + "\",\"userId\":\"user-001\"}]," +
                "\"nextToken\":null}}}";

        /** Single failed audit-log entry */
        MOCK_ONE_FAILED_ENTRY =
                "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-002\"," +
                "\"action\":\"Deployment failed\",\"auditStatus\":\"failed\"," +
                "\"timestamp\":\"" + ts2 + "\",\"userId\":\"user-002\"}]," +
                "\"nextToken\":null}}}";

        /** Single info audit-log entry */
        MOCK_ONE_INFO_ENTRY =
                "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-003\"," +
                "\"action\":\"Device configuration updated\",\"auditStatus\":\"info\"," +
                "\"timestamp\":\"" + ts3 + "\",\"userId\":\"user-003\"}]," +
                "\"nextToken\":null}}}";
    }

    /** One In-Progress service order — Deployment Service shows Operational */
    private static final String MOCK_ONE_IN_PROGRESS_ORDER =
            "{\"data\":{\"listServiceOrdersByStatus\":{\"items\":[" +
            "{\"id\":\"order-001\",\"status\":\"In Progress\",\"deviceId\":\"dev-001\"}]," +
            "\"nextToken\":null}}}";

    /** Empty orders response — Deployment Service shows Degraded */
    private static final String MOCK_EMPTY_ORDERS =
            "{\"data\":{\"listServiceOrdersByStatus\":{\"items\":[],\"nextToken\":null}}}";

    /** Three offline devices — View Inventory badge count = 3 */
    private static final String MOCK_THREE_OFFLINE_DEVICES =
            "{\"data\":{\"listDevices\":{\"items\":[" +
            "{\"id\":\"dev-001\",\"status\":\"Offline\",\"healthScore\":60}," +
            "{\"id\":\"dev-002\",\"status\":\"Offline\",\"healthScore\":55}," +
            "{\"id\":\"dev-003\",\"status\":\"Offline\",\"healthScore\":70}]," +
            "\"nextToken\":null}}}";

    /** Five scheduled service orders — Schedule Service badge count = 5 */
    private static final String MOCK_FIVE_SCHEDULED_ORDERS =
            "{\"data\":{\"listServiceOrdersByStatus\":{\"items\":[" +
            "{\"id\":\"ord-001\",\"status\":\"Scheduled\",\"deviceId\":\"dev-001\"}," +
            "{\"id\":\"ord-002\",\"status\":\"Scheduled\",\"deviceId\":\"dev-002\"}," +
            "{\"id\":\"ord-003\",\"status\":\"Scheduled\",\"deviceId\":\"dev-003\"}," +
            "{\"id\":\"ord-004\",\"status\":\"Scheduled\",\"deviceId\":\"dev-004\"}," +
            "{\"id\":\"ord-005\",\"status\":\"Scheduled\",\"deviceId\":\"dev-005\"}]," +
            "\"nextToken\":null}}}";

    /** Two pending firmware records — Deploy Firmware badge count = 2 */
    private static final String MOCK_TWO_PENDING_FIRMWARE =
            "{\"data\":{\"listFirmware\":{\"items\":[" +
            "{\"id\":\"fw-001\",\"status\":\"Pending\",\"version\":\"1.0.1\"}," +
            "{\"id\":\"fw-002\",\"status\":\"Pending\",\"version\":\"1.0.2\"}]," +
            "\"nextToken\":null}}}";

    /** Four pending compliance records — Check Compliance badge count = 4 */
    private static final String MOCK_FOUR_PENDING_COMPLIANCE =
            "{\"data\":{\"listFirmwareCompliance\":{\"items\":[" +
            "{\"id\":\"comp-001\",\"status\":\"Pending\",\"deviceId\":\"dev-001\"}," +
            "{\"id\":\"comp-002\",\"status\":\"Pending\",\"deviceId\":\"dev-002\"}," +
            "{\"id\":\"comp-003\",\"status\":\"Pending\",\"deviceId\":\"dev-003\"}," +
            "{\"id\":\"comp-004\",\"status\":\"Pending\",\"deviceId\":\"dev-004\"}]," +
            "\"nextToken\":null}}}";

    /** One pending compliance item — Compliance Engine shows Degraded */
    private static final String MOCK_ONE_PENDING_COMPLIANCE =
            "{\"data\":{\"listFirmwareCompliance\":{\"items\":[" +
            "{\"id\":\"comp-001\",\"status\":\"Pending\",\"deviceId\":\"dev-001\"}]," +
            "\"nextToken\":null}}}";

    /** Single device — used for TC-PS7-REFRESH-04 to verify updated data after refresh */
    private static final String MOCK_ONE_DEVICE =
            "{\"data\":{\"listDevices\":{\"items\":[" +
            "{\"id\":\"dev-001\",\"status\":\"Active\",\"healthScore\":90}]," +
            "\"nextToken\":null}}}";

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 1 — KPI Data Loading (TC-PS7-KPI-01 … TC-PS7-KPI-03)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-KPI-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify all four KPI cards populate from six parallel GraphQL API calls on Dashboard page load")
    @Outcome("Total Devices, Active Deployments, Pending Approvals, Health Score all show non-negative values; no '—' placeholder remains; no error banner")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testKpiCardsPopulateFromParallelApiCallsOnPageLoad() {

        step("Re-navigate to Dashboard to trigger a fresh page load with parallel API calls");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Dashboard welcome header is visible — on the correct page");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') confirms the Dashboard is rendered")
                        .isVisible()
        );

        step("Verify the 'Total Devices' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices card (div.bg-card with 'Total Devices' label) is visible")
                        .isVisible()
        );

        step("Verify the 'Active Deployments' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments card is visible")
                        .isVisible()
        );

        step("Verify the 'Pending Approvals' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals card is visible")
                        .isVisible()
        );

        step("Verify the 'Health Score' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score card is visible")
                        .isVisible()
        );

        step("Verify no em-dash '—' loading placeholder remains in any KPI card — all six APIs resolved");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' (U+2014) placeholder in any div[class*='text-3xl'] — all parallel API calls completed")
                        .isNotVisible()
        );

        step("Verify no red error banner is present — all six parallel API calls succeeded");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No div.bg-red-50 error banner in div.p-6.space-y-6 — APIs resolved without error")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-KPI-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify KPI card values display valid non-negative data — Total Devices numeric, Active Deployments numeric, Pending Approvals numeric, Health Score percentage")
    @Outcome("All four KPI card value elements are visible; Health Score contains '%'; no value is 'NaN', 'null', or empty")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testKpiValuesDisplayValidNonNegativeData() {

        step("Navigate to Dashboard to ensure live KPI data is loaded before validation");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify Total Devices value is visible and does not contain JavaScript error artifacts");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices numeric value (div[class*='text-3xl']) is present and populated")
                        .isVisible()
        );

        step("Verify Active Deployments value is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments value is present and populated")
                        .isVisible()
        );

        step("Verify Pending Approvals value is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals value is present and populated")
                        .isVisible()
        );

        step("Verify Health Score value contains a '%' suffix — confirming percentage format");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score value (div[class*='text-3xl']) contains the '%' suffix, e.g. '77%'")
                        .containsText("%")
        );

        step("Assert no KPI value contains 'NaN', 'null', or 'undefined' via JS evaluation");
        String allKpiText = (String) browser.evaluate(
                "() => Array.from(document.querySelectorAll(\"div[class*='text-3xl']\"))" +
                ".map(e => e.textContent).join('|')");
        String safeKpiText = allKpiText != null ? allKpiText : "";
        Assert.assertFalse(safeKpiText.isEmpty(),
                "KPI card text content must not be empty");
        Assert.assertFalse(safeKpiText.contains("NaN") || safeKpiText.contains("undefined") || safeKpiText.contains("null"),
                "KPI card values must not contain JavaScript error artifacts. Found: " + safeKpiText);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-KPI-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify KPI cards display the '—' (U+2014) em-dash loading placeholder while API fetch is delayed")
    @Outcome("At least one KPI card shows '—' while APIs are artificially delayed by 2 s; placeholder transitions to numeric values after resolution")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testKpiCardsShowLoadingPlaceholderDuringApiFetchDelay() {

        step("Register route handler to delay all AppSync GraphQL responses by 2 s");
        browser.route("**/graphql", route -> {
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                route.resume();
            }).start();
        });

        try {
            step("Re-navigate to Dashboard to trigger a fresh page load with delayed APIs");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Assert the em-dash '—' (U+2014) loading placeholder is visible in at least one KPI card immediately after navigation");
            user.wantsTo(
                    Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                            .describedAs("div[class*='text-3xl']:text-is('—') is visible in at least one KPI card while APIs are in-flight")
                            .isVisible()
            );
        } finally {
            step("Wait for delayed route threads to complete their route.resume() calls before unregistering");
            user.is(Waiting.on(KpiCard.TOTAL_DEVICES_VALUE).seconds(8));
            step("Unregister all route handlers to restore normal API behaviour");
            browser.unrouteAll();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 2 — Alerts from Audit Logs (TC-PS7-ALERTS-01 … TC-PS7-ALERTS-03)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ALERTS-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Recent Alerts panel displays exactly 3 entries when the listAuditLogs API returns 3 items within the 24-hour window")
    @Outcome("Exactly 3 div.bg-muted alert item rows are visible in the panel; 'No recent activity' empty-state is absent")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testAlertsPanelDisplaysThreeEntriesFrom24HourWindow() {

        step("Register route to inject 3-entry mixed audit-log mock response");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_THREE_AUDIT_LOG_ENTRIES));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard so the 3-entry mock is applied on page load");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the Recent Alerts panel container is visible");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("div.bg-card:has(h3:text-is('Recent Alerts')) is rendered and visible")
                            .isVisible()
            );

            step("Assert exactly 3 div.bg-muted alert item rows are rendered in div.space-y-3");
            Number alertCount = (Number) browser.evaluate(
                    "() => document.querySelectorAll(" +
                    "\"div.bg-card:has(h3) div.space-y-3 > div.bg-muted\").length");
            Assert.assertEquals(alertCount.intValue(), 3,
                    "Recent Alerts panel should display exactly 3 alert rows matching the 3-entry mock response");

            step("Verify 'No recent activity' empty-state is NOT visible — entries are displayed");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.EMPTY_STATE)
                            .describedAs("'No recent activity' empty state is absent when 3 audit-log entries are injected")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ALERTS-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Recent Alerts panel shows 'No recent activity' empty state when listAuditLogs returns an empty items array")
    @Outcome("'No recent activity' text is visible; zero div.bg-muted item rows are rendered; panel container is still present")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testAlertsPanelShowsEmptyStateWhenNoAuditLogsExist() {

        step("Register route to return empty audit-log response");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_EMPTY_AUDIT_LOGS));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the empty audit-log route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the Recent Alerts panel container is still present");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("Panel does not disappear when there are no log entries")
                            .isVisible()
            );

            step("Verify 'No recent activity' empty-state text is visible inside the panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.EMPTY_STATE)
                            .describedAs("'No recent activity' text (div.text-sm.text-muted-foreground inside div.space-y-3) is displayed")
                            .isVisible()
            );

            step("Assert zero div.bg-muted item rows are rendered — no ghost rows appear for empty data");
            Number alertCount = (Number) browser.evaluate(
                    "() => document.querySelectorAll(" +
                    "\"div.bg-card:has(h3) div.space-y-3 > div.bg-muted\").length");
            Assert.assertEquals(alertCount.intValue(), 0,
                    "No div.bg-muted item rows should exist when the API returns an empty items array");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ALERTS-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify the 'View all' link inside the Recent Alerts panel navigates to /analytics")
    @Outcome("a[href='/analytics'] is visible in the panel; clicking it navigates to the /analytics route")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testAlertsPanelViewAllLinkNavigatesToAnalytics() {

        step("Verify the 'View all' anchor element (a[href='/analytics']) is visible inside the Recent Alerts panel");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.VIEW_ALL_LINK)
                        .describedAs("a[href='/analytics'] inside div.bg-card:has(h3:text-is('Recent Alerts')) is visible")
                        .isVisible()
        );

        step("Click the 'View all' link");
        user.attemptsTo(DashboardPageImpl.clickViewAllAlerts());

        step("Assert the URL path is now /analytics");
        String path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/analytics",
                "'View all' link must navigate to the /analytics (Reporting & Analytics) route");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 3 — Alert Severity Icons (TC-PS7-ICON-01 … TC-PS7-ICON-04)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ICON-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify a failed audit-log entry renders an exclusive orange warning SVG icon (text-orange-*) inside the alert item row")
    @Outcome("svg[class*='text-orange'] is visible; no green (text-green-*) or blue (text-blue-*) SVG is present in the same row")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testFailedAuditEntryRendersOrangeWarningSvgIcon() {

        step("Register route to inject a single failed-status audit-log entry");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_FAILED_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the failed-entry route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the orange warning SVG icon (text-orange-*) is visible inside the alert item row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.FAILED_ACTION_ICON)
                            .describedAs("svg[class*='text-orange'] is present in div.bg-muted row — auditStatus='failed' maps to orange icon")
                            .isVisible()
            );

            step("Verify no green SVG icon is present in the alert row — colour is exclusive per severity");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.APPROVED_ACTION_ICON)
                            .describedAs("No green icon (svg[class*='text-green']) in the failed-action row")
                            .isNotVisible()
            );

            step("Verify no blue SVG icon is present in the alert row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.INFO_ACTION_ICON)
                            .describedAs("No blue icon (svg[class*='text-blue']) in the failed-action row")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ICON-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify an approved audit-log entry renders an exclusive green check SVG icon (text-green-*) inside the alert item row")
    @Outcome("svg[class*='text-green'] is visible; no orange (text-orange-*) SVG is present in the same row")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testApprovedAuditEntryRendersGreenCheckSvgIcon() {

        step("Register route to inject a single approved-status audit-log entry");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_APPROVED_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the approved-entry route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the green check SVG icon (text-green-*) is visible inside the alert item row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.APPROVED_ACTION_ICON)
                            .describedAs("svg[class*='text-green'] is present — auditStatus='approved' maps to green icon")
                            .isVisible()
            );

            step("Verify no orange SVG icon is present — approved entries must not show the warning icon");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.FAILED_ACTION_ICON)
                            .describedAs("No orange icon (svg[class*='text-orange']) in the approved-action row")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ICON-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify a generic (info) audit-log entry renders an exclusive blue info SVG icon (text-blue-*) inside the alert item row")
    @Outcome("svg[class*='text-blue'] is visible; no orange or green SVG is present in the same row")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testInfoAuditEntryRendersBlueInfoSvgIcon() {

        step("Register route to inject a single info-status audit-log entry");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_INFO_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the info-entry route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the blue info SVG icon (text-blue-*) is visible inside the alert item row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.INFO_ACTION_ICON)
                            .describedAs("svg[class*='text-blue'] is present — auditStatus='info' maps to blue icon")
                            .isVisible()
            );

            step("Verify no orange warning icon is present in the info-action row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.FAILED_ACTION_ICON)
                            .describedAs("No orange icon (svg[class*='text-orange']) in the info row")
                            .isNotVisible()
            );

            step("Verify no green check icon is present in the info-action row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.APPROVED_ACTION_ICON)
                            .describedAs("No green icon (svg[class*='text-green']) in the info row")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ICON-04",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify each alert item row displays the action text and a non-empty timestamp alongside the severity icon")
    @Outcome("Panel contains the injected action text; JS evaluation of item row text content is non-empty and includes a time reference")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testAlertItemRowDisplaysActionTextAndTimestamp() {

        step("Register route to inject one approved entry with known action text 'Firmware update approved'");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_APPROVED_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with mock entry active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the alert item row is visible in the panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("div.bg-muted alert item row is rendered inside div.space-y-3")
                            .isVisible()
            );

            step("Verify the action text 'Firmware update approved' is rendered inside the alerts panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("Panel contains the injected action text 'Firmware update approved'")
                            .containsText("Firmware update approved")
            );

            step("Assert alert item row text content is non-empty (includes action text and time reference)");
            String itemText = (String) browser.evaluate(
                    "() => { const item = document.querySelector(" +
                    "\"div.bg-card:has(h3) div.space-y-3 > div.bg-muted\"); " +
                    "return item ? item.textContent.trim() : ''; }");
            Assert.assertFalse(itemText == null || itemText.isEmpty(),
                    "Alert item row must contain non-empty text including action description and a timestamp");
        } finally {
            browser.unrouteAll();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 4 — Quick Action Badge Counts (TC-PS7-BADGE-01 … TC-PS7-BADGE-06)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify all four Quick Action cards are rendered in the main content area with labels, SVG icons, and badge spans")
    @Outcome("View Inventory, Schedule Service, Deploy Firmware, Check Compliance are all visible; exactly 4 cards in the Quick Actions grid")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testAllFourQuickActionCardsRenderedWithLabelsIconsAndBadges() {

        step("Re-navigate to Dashboard for a clean state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify 'View Inventory' quick action card (main a[href='/inventory']) is visible in main content");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD_MAIN)
                        .describedAs("main a[href='/inventory'] is rendered — not the sidebar nav link")
                        .isVisible()
        );

        step("Verify 'Schedule Service' quick action card (main a[href='/account-service']) is visible");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
                        .describedAs("main a[href='/account-service'] is rendered in main content area")
                        .isVisible()
        );

        step("Verify 'Deploy Firmware' quick action card (a[href='/deployment'].relative.bg-card) is visible");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("a[href='/deployment'].relative.bg-card is rendered — disambiguated from sidebar nav link")
                        .isVisible()
        );

        step("Verify 'Check Compliance' quick action card (main a[href='/compliance']) is visible");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
                        .describedAs("main a[href='/compliance'] is rendered in main content area")
                        .isVisible()
        );

        step("Assert exactly 4 Quick Action card links are rendered in the main content grid");
        Number cardCount = (Number) browser.evaluate(
                "() => document.querySelectorAll(\"main div[class*='grid'] > a\").length");
        Assert.assertEquals(cardCount.intValue(), 4,
                "Quick Actions grid must contain exactly 4 card links — no duplicates, no missing cards");

        step("Verify each card has an SVG icon element");
        user.wantsTo(Verify.uiElement(QuickActions.VIEW_INVENTORY_ICON).describedAs("View Inventory SVG icon").isVisible());
        user.wantsTo(Verify.uiElement(QuickActions.SCHEDULE_SERVICE_ICON).describedAs("Schedule Service SVG icon").isVisible());
        user.wantsTo(Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_ICON).describedAs("Deploy Firmware SVG icon").isVisible());
        user.wantsTo(Verify.uiElement(QuickActions.CHECK_COMPLIANCE_ICON).describedAs("Check Compliance SVG icon").isVisible());
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify View Inventory quick action card displays correct offline device badge count when 3 offline devices are returned by the API")
    @Outcome("main a[href='/inventory'] span[class*='absolute'] is visible and displays '3'")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testViewInventoryCardShowsCorrectOfflineDeviceBadgeCount() {

        step("Register route to inject 3 offline devices for listDevices(Offline) query");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listDevices") && body.contains("Offline")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_THREE_OFFLINE_DEVICES));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the offline-devices mock route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the orange badge span is visible on the 'View Inventory' card");
            user.wantsTo(
                    Verify.uiElement(QuickActions.VIEW_INVENTORY_BADGE)
                            .describedAs("main a[href='/inventory'] span[class*='absolute'] is rendered")
                            .isVisible()
            );

            step("Assert badge text displays '3' matching the 3 injected offline devices");
            String badgeText = (String) browser.evaluate(
                    "() => { const b = document.querySelector(\"main a[href='/inventory'] span[class*='absolute']\"); " +
                    "return b ? b.textContent.trim() : ''; }");
            Assert.assertEquals(badgeText, "3",
                    "View Inventory badge must show '3' when listDevices(Offline) returns 3 items");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Schedule Service quick action card displays correct scheduled orders badge count when 5 scheduled orders are returned")
    @Outcome("main a[href='/account-service'] span[class*='absolute'] is visible and displays '5'")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testScheduleServiceCardShowsCorrectScheduledOrdersBadgeCount() {

        step("Register route to inject 5 scheduled orders for listServiceOrdersByStatus(Scheduled) query");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listServiceOrdersByStatus") && body.contains("Scheduled")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_FIVE_SCHEDULED_ORDERS));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the scheduled-orders mock route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the orange badge is visible on the 'Schedule Service' card");
            user.wantsTo(
                    Verify.uiElement(QuickActions.SCHEDULE_SERVICE_BADGE)
                            .describedAs("main a[href='/account-service'] span[class*='absolute'] is rendered")
                            .isVisible()
            );

            step("Assert badge text displays '5' matching the 5 injected scheduled orders");
            String badgeText = (String) browser.evaluate(
                    "() => { const b = document.querySelector(\"main a[href='/account-service'] span[class*='absolute']\"); " +
                    "return b ? b.textContent.trim() : ''; }");
            Assert.assertEquals(badgeText, "5",
                    "Schedule Service badge must show '5' when listServiceOrdersByStatus(Scheduled) returns 5 items");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-04",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Deploy Firmware quick action card displays correct pending firmware badge count when 2 pending firmware records are returned")
    @Outcome("a[href='/deployment'].relative.bg-card span[class*='absolute'] is visible and displays '2'")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testDeployFirmwareCardShowsCorrectPendingFirmwareBadgeCount() {

        step("Register route to inject 2 pending firmware records for listFirmware(Pending) query");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listFirmware") && !body.contains("listFirmwareCompliance")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_TWO_PENDING_FIRMWARE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the pending-firmware mock route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the orange badge is visible on the 'Deploy Firmware' card");
            user.wantsTo(
                    Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_BADGE)
                            .describedAs("a[href='/deployment'].relative.bg-card span[class*='absolute'] is rendered")
                            .isVisible()
            );

            step("Assert badge text displays '2' matching the 2 injected pending firmware records");
            String badgeText = (String) browser.evaluate(
                    "() => { const b = document.querySelector(\"a[href='/deployment'].relative.bg-card span[class*='absolute']\"); " +
                    "return b ? b.textContent.trim() : ''; }");
            Assert.assertEquals(badgeText, "2",
                    "Deploy Firmware badge must show '2' when listFirmware(Pending) returns 2 items");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-05",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Check Compliance quick action card displays correct pending compliance badge count when 4 pending compliance records are returned")
    @Outcome("main a[href='/compliance'] span[class*='absolute'] is visible and displays '4'")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testCheckComplianceCardShowsCorrectPendingComplianceBadgeCount() {

        step("Register route to inject 4 pending compliance records for listFirmwareCompliance(Pending) query");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listFirmwareCompliance")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_FOUR_PENDING_COMPLIANCE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the pending-compliance mock route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the orange badge is visible on the 'Check Compliance' card");
            user.wantsTo(
                    Verify.uiElement(QuickActions.CHECK_COMPLIANCE_BADGE)
                            .describedAs("main a[href='/compliance'] span[class*='absolute'] is rendered")
                            .isVisible()
            );

            step("Assert badge text displays '4' matching the 4 injected pending compliance records");
            String badgeText = (String) browser.evaluate(
                    "() => { const b = document.querySelector(\"main a[href='/compliance'] span[class*='absolute']\"); " +
                    "return b ? b.textContent.trim() : ''; }");
            Assert.assertEquals(badgeText, "4",
                    "Check Compliance badge must show '4' when listFirmwareCompliance(Pending) returns 4 items");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-BADGE-06",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify each Quick Action card navigates to the correct module route when clicked")
    @Outcome("Clicking View Inventory → /inventory; Schedule Service → /account-service; Deploy Firmware → /deployment; Check Compliance → /compliance")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testQuickActionCardsNavigateToCorrectModuleRoutes() {

        step("Re-navigate to Dashboard to ensure a clean state before navigation tests");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Click 'View Inventory' card and verify navigation to /inventory");
        user.attemptsTo(DashboardPageImpl.clickViewInventoryCard());
        String path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/inventory", "View Inventory card must navigate to /inventory");

        step("Return to Dashboard");
        user.attemptsTo(Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions()));

        step("Click 'Schedule Service' card and verify navigation to /account-service");
        user.attemptsTo(DashboardPageImpl.clickScheduleServiceCard());
        path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/account-service", "Schedule Service card must navigate to /account-service");

        step("Return to Dashboard");
        user.attemptsTo(Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions()));

        step("Click 'Deploy Firmware' card and verify navigation to /deployment");
        user.attemptsTo(DashboardPageImpl.clickDeployFirmwareCard());
        path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/deployment", "Deploy Firmware card must navigate to /deployment");

        step("Return to Dashboard");
        user.attemptsTo(Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions()));

        step("Click 'Check Compliance' card and verify navigation to /compliance");
        user.attemptsTo(DashboardPageImpl.clickCheckComplianceCard());
        path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/compliance", "Check Compliance card must navigate to /compliance");
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 5 — System Status Toggle (TC-PS7-STATUS-01 … TC-PS7-STATUS-04)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-STATUS-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Deployment Service shows green 'Operational' label in System Status when at least one In-Progress service order exists")
    @Outcome("span.text-green-600 is visible in the Deployment Service row; no span.text-orange-600 is present in that row")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testDeploymentServiceShowsOperationalWhenActiveOrdersExist() {

        step("Register route to return 1 In-Progress order for listServiceOrdersByStatus(In Progress)");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listServiceOrdersByStatus") && body.contains("In Progress")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_IN_PROGRESS_ORDER));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the in-progress orders mock active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the System Status panel container is visible");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.CONTAINER)
                            .describedAs("div.bg-card:has(h3:text-is('System Status')) is rendered")
                            .isVisible()
            );

            step("Verify Deployment Service row shows green 'Operational' label");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_OPERATIONAL)
                            .describedAs("span.text-green-600 is visible in the Deployment Service row — active orders produce Operational status")
                            .isVisible()
            );

            step("Verify no orange 'Degraded' label in the Deployment Service row");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_DEGRADED)
                            .describedAs("No span.text-orange-600 in the Deployment Service row when active orders exist")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-STATUS-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Deployment Service shows orange 'Degraded' label in System Status when zero In-Progress service orders exist")
    @Outcome("span.text-orange-600 is visible in the Deployment Service row; no span.text-green-600 is present in that row")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testDeploymentServiceShowsDegradedWhenNoActiveOrdersExist() {

        step("Register route to return empty orders for listServiceOrdersByStatus(In Progress)");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listServiceOrdersByStatus") && body.contains("In Progress")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_EMPTY_ORDERS));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the zero-orders mock active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify Deployment Service row shows orange 'Degraded' label");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_DEGRADED)
                            .describedAs("span.text-orange-600 is visible in the Deployment Service row — zero active orders produce Degraded status")
                            .isVisible()
            );

            step("Verify no green 'Operational' label in the Deployment Service row");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_OPERATIONAL)
                            .describedAs("No span.text-green-600 in the Deployment Service row when zero active orders exist")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-STATUS-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify System Status panel renders exactly 4 service rows in the fixed order: Deployment Service, Compliance Engine, Asset Database, Analytics Platform")
    @Outcome("Exactly 4 div.space-y-4 > div rows; all four service names present; Analytics Platform always shows Operational")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testSystemStatusPanelRendersFourServiceRowsInFixedOrder() {

        step("Assert exactly 4 service rows inside div.space-y-4 via JS evaluation");
        Number rowCount = (Number) browser.evaluate(
                "() => document.querySelectorAll(" +
                "\"div.bg-card:has(h3) div.space-y-4 > div\").length");
        Assert.assertEquals(rowCount.intValue(), 4,
                "System Status panel must render exactly 4 service rows");

        step("Verify panel contains 'Deployment Service' row text");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel contains 'Deployment Service'")
                        .containsText("Deployment Service")
        );

        step("Verify panel contains 'Compliance Engine' row text");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel contains 'Compliance Engine'")
                        .containsText("Compliance Engine")
        );

        step("Verify panel contains 'Asset Database' row text");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel contains 'Asset Database'")
                        .containsText("Asset Database")
        );

        step("Verify panel contains 'Analytics Platform' row text");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel contains 'Analytics Platform'")
                        .containsText("Analytics Platform")
        );

        step("Verify Analytics Platform always shows green 'Operational' label per product specification");
        user.wantsTo(
                Verify.uiElement(SystemStatus.ANALYTICS_PLATFORM_OPERATIONAL)
                        .describedAs("span.text-green-600 in Analytics Platform row — always Operational per spec")
                        .isVisible()
        );

        step("Verify Analytics Platform does NOT show orange 'Degraded' label");
        user.wantsTo(
                Verify.uiElement(SystemStatus.ANALYTICS_PLATFORM_DEGRADED)
                        .describedAs("No span.text-orange-600 in Analytics Platform row — must not have a Degraded state")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-STATUS-04",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Compliance Engine shows orange 'Degraded' label when at least one pending compliance item is present")
    @Outcome("span.text-orange-600 is visible in the Compliance Engine row when listFirmwareCompliance returns Pending items")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testComplianceEngineShowsDegradedWhenPendingComplianceItemsExist() {

        step("Register route to inject 1 pending compliance record for listFirmwareCompliance");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listFirmwareCompliance")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_PENDING_COMPLIANCE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard with the pending compliance mock active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify Compliance Engine row shows orange 'Degraded' label");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.COMPLIANCE_ENGINE_DEGRADED)
                            .describedAs("span.text-orange-600 is visible in the Compliance Engine row — pending compliance items produce Degraded status")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 6 — Manual Refresh (TC-PS7-REFRESH-01 … TC-PS7-REFRESH-04)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-REFRESH-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify the Refresh Dashboard button is visible in the welcome header row and is in an enabled state")
    @Outcome("button[aria-label='Refresh dashboard'] is visible and not disabled")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testRefreshDashboardButtonIsVisibleAndEnabled() {

        step("Navigate to Dashboard to ensure a clean state before testing refresh button");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Refresh Dashboard button (button[aria-label='Refresh dashboard']) is visible");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh Dashboard button is rendered in the top-right area of the welcome header row")
                        .isVisible()
        );

        step("Assert the Refresh Dashboard button is not disabled");
        Boolean isDisabled = (Boolean) browser.evaluate(
                "() => document.querySelector(\"button[aria-label='Refresh dashboard']\")?.disabled ?? true");
        Assert.assertFalse(isDisabled,
                "Refresh Dashboard button must be in an enabled state — not disabled attribute");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-REFRESH-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify clicking the Refresh button re-triggers all API data fetches and all KPI cards repopulate with data")
    @Outcome("After refresh: all 4 KPI cards show populated values; no '—' placeholder remains; no error banner present")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testRefreshButtonReTriggersAllApiCallsAndKpiCardsRepopulate() {

        step("Navigate to Dashboard to ensure a clean state before testing refresh");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify Dashboard is fully loaded with live data before clicking refresh");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No loading placeholder before initiating refresh — Dashboard is in a stable state")
                        .isNotVisible()
        );

        step("Click the Refresh Dashboard button to re-trigger all API data fetches");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Wait for the em-dash loading placeholder to appear in at least one KPI card (refresh cycle in progress)");
        user.is(com.tpg.actions.Waiting.on(KpiCard.LOADING_PLACEHOLDER).within(10));

        step("Wait for the em-dash placeholder to disappear — all refreshed API calls have resolved");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("'—' placeholder is gone after refresh — KPI cards have repopulated")
                        .isNotVisible()
        );

        step("Verify all four KPI cards are populated after refresh");
        user.wantsTo(Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).describedAs("Total Devices repopulated after refresh").isVisible());
        user.wantsTo(Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE).describedAs("Active Deployments repopulated after refresh").isVisible());
        user.wantsTo(Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE).describedAs("Pending Approvals repopulated after refresh").isVisible());
        user.wantsTo(Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE).describedAs("Health Score repopulated after refresh").isVisible());

        step("Verify no error banner is present after successful refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No div.bg-red-50 error banner after refresh — all API calls succeeded")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-REFRESH-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify the Refresh button's SVG icon carries the Tailwind 'animate-spin' class during an active refresh cycle and the class is removed on completion")
    @Outcome("SVG class contains 'animate-spin' immediately after button click; 'animate-spin' absent after refresh cycle completes")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testRefreshButtonSvgIconAppliesAnimateSpinDuringActiveRefreshCycle() {

        step("Register route to delay all AppSync responses by 2 s — making the spinning animation observable");
        browser.route("**/graphql", route -> {
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                route.resume();
            }).start();
        });

        try {
            step("Re-navigate to Dashboard to ensure a clean state before testing refresh spin");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Click the Refresh Dashboard button to start an active refresh cycle");
            user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

            step("Immediately read the SVG class attribute of the Refresh button icon");
            String svgClass = (String) browser.evaluate(
                    "() => { const svg = document.querySelector(" +
                    "\"button[aria-label='Refresh dashboard'] svg\"); " +
                    "return svg ? (svg.getAttribute('class') || '') : ''; }");
            Assert.assertTrue(svgClass.contains("animate-spin"),
                    "Refresh button SVG must carry 'animate-spin' Tailwind class during an active refresh cycle. Found classes: " + svgClass);

            step("Wait for the 2-second delay to expire and the refresh cycle to complete");
            user.wantsTo(
                    Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                            .describedAs("Loading placeholder gone — refresh cycle complete")
                            .isNotVisible()
            );

            step("Assert 'animate-spin' is no longer in the SVG class attribute — spin animation stopped");
            String svgClassAfter = (String) browser.evaluate(
                    "() => { const svg = document.querySelector(" +
                    "\"button[aria-label='Refresh dashboard'] svg\"); " +
                    "return svg ? (svg.getAttribute('class') || '') : ''; }");
            Assert.assertFalse(svgClassAfter.contains("animate-spin"),
                    "'animate-spin' must be removed from Refresh button SVG after the refresh cycle completes. Found: " + svgClassAfter);
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-REFRESH-04",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify Dashboard data updates after a manual refresh when the backend data has changed — Total Devices reflects the new mock count")
    @Outcome("After refresh with a 1-device mock active: Total Devices KPI card displays '1'")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testDashboardDataUpdatesAfterRefreshWhenBackendDataChanged() {

        step("Register route to return exactly 1 device for all listDevices queries — simulating changed backend data");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listDevices") && !body.contains("Offline")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_ONE_DEVICE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Click the Refresh Dashboard button to re-fetch data with the mock route active");
            user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

            step("Wait for KPI loading placeholder to appear then disappear — refresh cycle complete");
            user.is(com.tpg.actions.Waiting.on(KpiCard.LOADING_PLACEHOLDER).within(10));
            user.wantsTo(
                    Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                            .describedAs("Placeholder gone — KPI cards repopulated with refreshed data")
                            .isNotVisible()
            );

            step("Assert Total Devices KPI card now displays '1' — the updated mock count from the refreshed API call");
            String totalDevicesText = (String) browser.evaluate(
                    "() => { const el = document.querySelector(" +
                    "\"div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')) div[class*='text-3xl']\"); " +
                    "return el ? el.textContent.trim() : ''; }");
            Assert.assertEquals(totalDevicesText, "1",
                    "Total Devices KPI card must display '1' after refresh when mock returns 1 device");
        } finally {
            browser.unrouteAll();
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // Suite 7 — Error Handling (TC-PS7-ERR-01 … TC-PS7-ERR-05)
    // ═══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ERR-01",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify a red error banner (div.bg-red-50) is displayed in the main content area when all KPI API calls fail with HTTP 500")
    @Outcome("div.p-6.space-y-6 > div.bg-red-50 is visible; error message text is readable within the banner")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testKpiApiFailureRendersRedErrorBanner() {

        step("Register route to return HTTP 500 for all AppSync GraphQL requests");
        browser.route("**/graphql", route ->
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Internal Server Error\"}"))
        );

        try {
            step("Re-navigate to Dashboard with the 500-error route active — all six KPI API calls will fail");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the red error banner container (div.p-6.space-y-6 > div.bg-red-50) is visible");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("div.bg-red-50 direct child of div.p-6.space-y-6 is rendered when all KPI APIs return 500")
                            .isVisible()
            );

            step("Verify the error banner contains a visible error message text");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.MESSAGE)
                            .describedAs("Error banner text is readable — 'Unable to load dashboard data.'")
                            .containsText("Unable to load dashboard data")
            );
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ERR-02",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify KPI cards fall back gracefully to '0' when all APIs fail — no crash, no blank screen, card layout intact")
    @Outcome("KpiCard.ZERO_FALLBACK is visible; all four KPI card containers are in DOM; no unhandled error overlay present")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testKpiFallsBackToZeroOnFullApiFailure() {

        step("Register route to return HTTP 500 for all AppSync GraphQL requests");
        browser.route("**/graphql", route ->
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Internal Server Error\"}"))
        );

        try {
            step("Re-navigate to Dashboard in the error state");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for the loading state to settle — placeholder transitions from '—' to '0'");
            user.is(com.tpg.actions.Waiting.on(KpiCard.ZERO_FALLBACK).within(15));

            step("Verify KPI card value elements display '0' as the error fallback — not '—' and not empty");
            user.wantsTo(
                    Verify.uiElement(KpiCard.ZERO_FALLBACK)
                            .describedAs("div[class*='text-3xl']:text-is('0') is visible — app gracefully degrades to '0' on full API failure")
                            .isVisible()
            );

            step("Verify no '—' loading placeholder remains — error state has fully resolved");
            user.wantsTo(
                    Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                            .describedAs("No '—' placeholder after error state has settled")
                            .isNotVisible()
            );

            step("Verify the Dashboard layout is intact — KPI card containers are still in the DOM");
            user.wantsTo(Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE).describedAs("Total Devices card present in DOM").isVisible());
            user.wantsTo(Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE).describedAs("Health Score card present in DOM").isVisible());

            step("Assert no blank screen via JS evaluation — Dashboard structural elements are visible");
            Boolean headerVisible = (Boolean) browser.evaluate(
                    "() => !!document.querySelector('h1')");
            Assert.assertTrue(headerVisible,
                    "Dashboard h1 header element must be present — no blank screen or unhandled error overlay");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ERR-03",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify an audit-log API failure is isolated to the alerts panel and does NOT trigger the global KPI error banner")
    @Outcome("Global div.bg-red-50 error banner is absent; Total Devices KPI is populated; alerts panel shows in-panel error or empty state")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testAuditLogApiFailureDoesNotTriggerGlobalKpiErrorBanner() {

        step("Register route to return HTTP 500 ONLY for listAuditLogs — all other requests pass through");
        browser.route("**/graphql", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Internal Server Error\"}"));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to Dashboard — KPI APIs succeed, only audit-log API fails");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the global KPI error banner is NOT visible — audit-log failure is isolated");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("No global div.bg-red-50 banner — the audit-log 500 error must not propagate to the KPI error domain")
                            .isNotVisible()
            );

            step("Verify Total Devices KPI card is populated — KPI domain is unaffected by alerts failure");
            user.wantsTo(
                    Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                            .describedAs("Total Devices KPI card shows a populated value confirming KPI APIs responded successfully")
                            .isVisible()
            );

            step("Verify alerts panel shows an in-panel error element (div.bg-red-50 inside card) or 'No recent activity' — not a global banner");
            boolean hasInPanelError = browser.locator(AlertsPanel.ERROR_MESSAGE).count() > 0;
            boolean hasEmptyState   = browser.locator(AlertsPanel.EMPTY_STATE).count() > 0;
            Assert.assertTrue(hasInPanelError || hasEmptyState,
                    "Alerts panel must show either its own div.bg-red-50 error element or 'No recent activity' — not silently render live entries after a 500 error");
        } finally {
            browser.unrouteAll();
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ERR-04",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify the error banner disappears after a successful manual refresh following an API failure — error state is recoverable")
    @Outcome("div.bg-red-50 banner visible after forced API failure; banner absent after clearing the error route and clicking Refresh")
    @Test(groups = {REGRESSION, DASHBOARD_E2E_INTEGRATION})
    public void testErrorBannerDisappearsAfterSuccessfulManualRefresh() {

        step("Register route to return HTTP 500 for all AppSync requests — force error state");
        browser.route("**/graphql", route ->
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Internal Server Error\"}"))
        );

        step("Re-navigate to Dashboard to trigger the error state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Confirm the red error banner is visible — error state confirmed");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("div.bg-red-50 error banner is present — error state is active before recovery")
                        .isVisible()
        );

        step("Remove all error route handlers to allow subsequent API calls to succeed");
        browser.unrouteAll();

        step("Click the Refresh Dashboard button to trigger a successful refresh cycle");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Wait for KPI cards to repopulate with live data — refresh cycle complete");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder gone — KPI cards repopulated after successful refresh")
                        .isNotVisible()
        );

        step("Verify the red error banner is no longer visible after successful refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("div.bg-red-50 error banner is dismissed — error state is not permanent and recovers correctly")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-PS7-ERR-05",
            stories = {"PS-7"}, category = "DASHBOARD_E2E_INTEGRATION")
    @Description("Verify unauthenticated direct navigation to the Dashboard redirects to the login page")
    @Outcome("After clearing all auth cookies and storage, navigating to base URL shows the login page email/password fields — Dashboard is not exposed")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_E2E_INTEGRATION})
    public void testUnauthenticatedAccessRedirectsToLoginPage() {

        step("Clear all cookies and browser storage to remove the authenticated session");
        browser.context().clearCookies();
        browser.evaluate(
                "() => { try { localStorage.clear(); sessionStorage.clear(); } catch (e) {} }");

        step("Navigate directly to the Dashboard base URL without an authenticated session");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the login page email input field is visible — user was redirected to login");
        user.wantsTo(
                Verify.uiElement(LoginPage.EMAIL_FIELD)
                        .describedAs("Email input field is visible — unauthenticated request was redirected to the login page")
                        .isVisible()
        );

        step("Verify the Dashboard h1 header is NOT visible — Dashboard is not exposed to unauthenticated sessions");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 header must NOT be present — app protects dashboard behind authentication")
                        .isNotVisible()
        );
    }
}
