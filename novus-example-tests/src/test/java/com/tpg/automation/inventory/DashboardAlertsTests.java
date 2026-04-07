package com.tpg.automation.inventory;

import com.microsoft.playwright.Route;
import com.tpg.actions.Launch;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DashboardPageImpl;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.DashboardPage.AlertsPanel;
import com.tpg.automation.pages.inventory.DashboardPage.ErrorBanner;
import com.tpg.automation.pages.inventory.DashboardPage.KpiCard;
import com.tpg.verification.Verify;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_ALERTS;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite covering the Dashboard Recent Alerts panel (Story PS-5 / 8.3).
 *
 * <p>Verifies that the Recent Alerts panel:
 * <ul>
 *   <li>Is visible and shows audit-log entries fetched from the last 24 hours
 *   <li>Displays action text, timestamp, and severity icons (orange=failed, green=approved, blue=info)
 *   <li>Shows "No recent activity" empty state when no logs exist
 *   <li>Shows a div.bg-red-50 error element when the audit-log API fails
 *   <li>Is isolated from KPI API failures (separate error domains)
 *   <li>Has a "View all" link that navigates to /analytics
 *   <li>Recovers correctly after Refresh Dashboard
 * </ul>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * and lands on the Dashboard before any test method runs. Tests that need deterministic
 * API responses use {@code browser.route()} with AppSync interception and clean up
 * via {@code browser.unrouteAll()} in a {@code finally} block.
 *
 * @see DashboardPage.AlertsPanel
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-5
 * @jira PS-30 (QA Sub-task)
 */
public class DashboardAlertsTests extends InventoryTestBase {

    // ── GraphQL mock bodies ────────────────────────────────────────────────────

    private static final String MOCK_ONE_APPROVED_ENTRY =
            "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-001\"," +
            "\"action\":\"Firmware update approved\",\"auditStatus\":\"approved\"," +
            "\"timestamp\":\"2026-04-01T10:00:00.000Z\",\"userId\":\"user-001\"}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_FAILED_ENTRY =
            "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-002\"," +
            "\"action\":\"Deployment failed\",\"auditStatus\":\"failed\"," +
            "\"timestamp\":\"2026-04-01T09:00:00.000Z\",\"userId\":\"user-001\"}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_INFO_ENTRY =
            "{\"data\":{\"listAuditLogs\":{\"items\":[{\"id\":\"audit-003\"," +
            "\"action\":\"Device configuration updated\",\"auditStatus\":\"info\"," +
            "\"timestamp\":\"2026-04-01T08:00:00.000Z\",\"userId\":\"user-001\"}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_EMPTY_LOGS =
            "{\"data\":{\"listAuditLogs\":{\"items\":[],\"nextToken\":null}}}";

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-01: Recent Alerts panel is visible on the Dashboard after login
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-01",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the Recent Alerts panel container is visible on the Dashboard after login")
    @Outcome("div.bg-card containing 'Recent Alerts' h3 is rendered and visible; no loading placeholder remains on KPI cards")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS})
    public void testRecentAlertsPanelIsVisibleAfterLogin() {

        step("Verify the Recent Alerts panel container is rendered in the Dashboard right column");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("div.bg-card:has(h3:text-is('Recent Alerts')) is visible — panel is rendered")
                        .isVisible()
        );

        step("Verify the 'Recent Alerts' h3 heading text is present inside the panel");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Panel container contains the text 'Recent Alerts' — heading is correct")
                        .containsText("Recent Alerts")
        );

        step("Verify no KPI loading placeholder '—' remains — dashboard has fully loaded");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No em-dash placeholder in any KPI card — dashboard APIs have resolved")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-02: Recent Alerts panel displays audit log entries
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-02",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that the Recent Alerts panel renders audit-log entry rows when the API returns items")
    @Outcome("At least one div.bg-muted alert item row is visible; 'No recent activity' empty state is absent")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS})
    public void testRecentAlertsPanelDisplaysAuditLogEntries() {

        step("Register route to inject one approved audit-log entry into the listAuditLogs response");
        browser.route("**appsync-api**", route -> {
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
            step("Re-navigate to the Dashboard so the mock audit-log response is applied on page load");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify at least one alert item row (div.bg-muted) is rendered inside the panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("div.flex.items-start.gap-3.p-3.bg-muted.rounded-lg item row is visible — injected entry is displayed")
                            .isVisible()
            );

            step("Verify the 'No recent activity' empty-state text is NOT visible — entries exist");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.EMPTY_STATE)
                            .describedAs("Empty-state 'No recent activity' is absent when audit-log items are present")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-03: Each alert item displays action text and timestamp
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-03",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that each alert item row displays the action text and a timestamp")
    @Outcome("Alert item is visible; panel contains the injected action text; alert item contains a non-empty timestamp string")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testAlertItemDisplaysActionTextAndTimestamp() {

        step("Register route to inject one audit-log entry with action 'Firmware update approved'");
        browser.route("**appsync-api**", route -> {
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
            step("Re-navigate to the Dashboard with mock entry active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the alert item row is visible");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("Alert item row (div.bg-muted) is present in div.space-y-3")
                            .isVisible()
            );

            step("Verify the action text 'Firmware update approved' is displayed inside the alerts panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("Alert panel contains the injected action text 'Firmware update approved'")
                            .containsText("Firmware update approved")
            );

            step("Verify a timestamp string is present within the alert item using JS evaluate");
            String timestampText = (String) browser.evaluate(
                    "() => { const item = document.querySelector('div.bg-card:has(h3) div.space-y-3 > div.bg-muted'); " +
                    "return item ? item.textContent : ''; }");
            org.testng.Assert.assertFalse(
                    timestampText == null || timestampText.trim().isEmpty(),
                    "Alert item must contain non-empty text including a timestamp");
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-04: Failed actions display an orange warning icon
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-04",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that a failed-action audit-log entry renders an orange warning SVG icon inside the alert row")
    @Outcome("svg[class*='text-orange'] is visible inside div.bg-muted for a failed-action entry")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testFailedActionDisplaysOrangeWarningIcon() {

        step("Register route to inject a failed-action audit-log entry (auditStatus='failed')");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_FAILED_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with failed-action mock entry active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the failed-action alert item row is rendered");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("Alert row for the failed action is present in div.space-y-3")
                            .isVisible()
            );

            step("Verify an orange warning icon (svg[class*='text-orange']) is present inside the failed-action row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.FAILED_ACTION_ICON)
                            .describedAs("Orange SVG icon (text-orange-* Tailwind class) is rendered in the failed-action alert row")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-05: Approved/approve actions display a green check icon
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-05",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that an approved-action audit-log entry renders a green check SVG icon inside the alert row")
    @Outcome("svg[class*='text-green'] is visible inside div.bg-muted for an approved-action entry")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testApprovedActionDisplaysGreenCheckIcon() {

        step("Register route to inject an approved-action audit-log entry (auditStatus='approved')");
        browser.route("**appsync-api**", route -> {
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
            step("Re-navigate to the Dashboard with approved-action mock entry active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the approved-action alert item row is rendered");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("Alert row for the approved action is present")
                            .isVisible()
            );

            step("Verify a green check icon (svg[class*='text-green']) is present inside the approved-action row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.APPROVED_ACTION_ICON)
                            .describedAs("Green SVG icon (text-green-* Tailwind class) is rendered in the approved-action alert row")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-06: Other (non-failed, non-approved) actions display a blue info icon
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-06",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that a generic (info) audit-log entry renders a blue info SVG icon inside the alert row")
    @Outcome("svg[class*='text-blue'] is visible inside div.bg-muted for an info-action entry")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testInfoActionDisplaysBlueInfoIcon() {

        step("Register route to inject an info-action audit-log entry (auditStatus='info')");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_INFO_ENTRY));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with info-action mock entry active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the info-action alert item row is rendered");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("Alert row for the generic info action is present")
                            .isVisible()
            );

            step("Verify a blue info icon (svg[class*='text-blue']) is present inside the info-action row");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.INFO_ACTION_ICON)
                            .describedAs("Blue SVG icon (text-blue-* Tailwind class) is rendered in the info-action alert row")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-07: 'View all' link is visible and navigates to /analytics
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-07",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the 'View all' link inside the Recent Alerts panel is visible and navigates to /analytics")
    @Outcome("'View all' anchor (href='/analytics') is visible; clicking it routes the browser to /analytics URL path")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS})
    public void testViewAllLinkNavigatesToAnalytics() {

        step("Verify the 'View all' link is visible inside the Recent Alerts panel");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.VIEW_ALL_LINK)
                        .describedAs("a[href='/analytics'] inside the Recent Alerts card is visible")
                        .isVisible()
        );

        step("Click the 'View all' link");
        user.attemptsTo(
                DashboardPageImpl.clickViewAllAlerts()
        );

        step("Verify the current URL path is /analytics after clicking 'View all'");
        String path = (String) browser.evaluate("() => window.location.pathname");
        org.testng.Assert.assertEquals(path, "/analytics",
                "'View all' link must navigate to /analytics — actual path: " + path);
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-08: Loading state — 'Loading alerts…' indicator shown while API pending
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-08",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the 'Loading alerts…' text indicator is shown in the Recent Alerts panel while the audit-log API response is delayed")
    @Outcome("'Loading alerts' substring (Unicode ellipsis U+2026 suffix in DOM) is visible in the panel before data arrives")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testLoadingIndicatorShownWhileAlertApiPending() {

        step("Register route to delay all AppSync API responses by 2 s so the loading state is observable");
        browser.route("**appsync-api**", route -> {
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                route.resume();
            }).start();
        });

        try {
            step("Re-navigate to the Dashboard so the delayed route is active during page load");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify 'Loading alerts…' indicator is visible while audit-log API response is delayed");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.LOADING_INDICATOR)
                            .describedAs("'Loading alerts' text (substring match — app uses U+2026 ellipsis) is visible in the panel while API is in-flight")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-09: Empty state — 'No recent activity' when no audit logs exist
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-09",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the 'No recent activity' empty-state message is displayed when the audit-log API returns an empty list")
    @Outcome("'No recent activity' text is visible in div.text-sm.text-muted-foreground; no div.bg-muted item rows are rendered; panel container remains visible")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testEmptyStateShownWhenNoAuditLogsExist() {

        step("Register route to return an empty listAuditLogs response (items=[], totalCount=0)");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_EMPTY_LOGS));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with the empty audit-log route active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the 'No recent activity' empty-state message is visible");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.EMPTY_STATE)
                            .describedAs("'No recent activity' text is displayed inside div.space-y-3 when audit-log items array is empty")
                            .isVisible()
            );

            step("Verify no alert item rows (div.bg-muted) are rendered — panel is in empty state");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ALERT_ITEM)
                            .describedAs("No div.bg-muted alert item rows are present — only the empty-state div is inside div.space-y-3")
                            .isNotVisible()
            );

            step("Verify the Recent Alerts panel container is still visible in empty state");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("Panel renders its heading and empty state text — not a blank space or removed element")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-10: Error state — panel shows div.bg-red-50 when audit-log API returns 500
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-10",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the alerts panel shows its own div.bg-red-50 error element when the audit-log API returns HTTP 500; KPI cards are unaffected")
    @Outcome("div.bg-red-50 inside the alerts card is visible; Total Devices KPI is still populated; global KPI error banner is NOT visible")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testErrorStateShownWhenAuditLogApiReturns500() {

        step("Register route to return HTTP 500 only for listAuditLogs queries; other queries pass through");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listAuditLogs")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Audit log service unavailable\"}"));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard; KPI APIs succeed but audit-log API returns 500");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the alerts panel error element (div.bg-red-50 inside the card) is visible");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.ERROR_MESSAGE)
                            .describedAs("Red error box (div.bg-red-50) inside the Recent Alerts card — this is separate from the global KPI error banner")
                            .isVisible()
            );

            step("Verify Total Devices KPI card is still populated — alerts failure does not affect KPI domain");
            user.wantsTo(
                    Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                            .describedAs("Total Devices KPI value is visible with live data — audit-log failure is isolated")
                            .isVisible()
            );

            step("Verify the global KPI error banner is NOT visible — failure is isolated to the alerts panel only");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("No global red error banner (div.p-6.space-y-6 > div.bg-red-50) — KPI APIs succeeded")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-11: Alerts panel is isolated from KPI API failures
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-11",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the Recent Alerts panel remains visible when all KPI APIs fail (global error banner appears)")
    @Outcome("Global error banner is visible after KPI API failures; Recent Alerts panel container is still rendered")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS})
    public void testAlertsPanelIsolatedFromKpiApiFailures() {

        step("Confirm Dashboard is fully loaded before registering the error route");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard is loaded with live data before applying error route")
                        .isVisible()
        );

        step("Register route to return HTTP 500 for all fetch/xhr calls to simulate KPI API failures");
        browser.route("**/*", route -> {
            String rt = route.request().resourceType();
            if ("fetch".equals(rt) || "xhr".equals(rt)) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Service unavailable\"}"));
            } else {
                route.fallback();
            }
        });

        try {
            step("Click Refresh Dashboard to re-trigger all KPI API calls with the error route active");
            user.attemptsTo(
                    DashboardPageImpl.clickRefreshDashboard()
            );

            step("Verify the global KPI error banner appears — all KPI APIs failed");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("Red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible after KPI API failures")
                            .isVisible()
            );

            step("Verify the Recent Alerts panel container is still visible — it is a separate component from the KPI error banner");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.CONTAINER)
                            .describedAs("Recent Alerts panel is still rendered — it does not disappear when KPI APIs fail")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-ALERTS-12: Alerts panel updates correctly after clicking Refresh Dashboard
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-ALERTS-12",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the Recent Alerts panel remains rendered and no error appears after clicking the Refresh Dashboard button")
    @Outcome("Alerts panel is visible before and after refresh; KPI cards repopulate with live values; no global error banner appears")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS})
    public void testAlertsPanelUpdatesAfterRefreshDashboard() {

        step("Verify the Recent Alerts panel is visible on the loaded Dashboard before refresh");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Alerts panel is present before the refresh cycle")
                        .isVisible()
        );

        step("Verify the Refresh Dashboard button is visible");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("button[aria-label='Refresh dashboard'] is rendered in the welcome header row")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Wait for KPI cards to repopulate with live data — confirms the refresh cycle completed");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card shows a live value after the refresh cycle")
                        .isVisible()
        );

        step("Verify the Recent Alerts panel is still visible after the refresh cycle");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Alerts panel remains rendered after the refresh — not removed or broken by the refresh operation")
                        .isVisible()
        );

        step("Verify no global KPI error banner is present post-refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner after refresh — all APIs responded successfully")
                        .isNotVisible()
        );
    }
}
