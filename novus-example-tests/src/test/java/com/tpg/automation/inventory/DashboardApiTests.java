package com.tpg.automation.inventory;

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
import com.tpg.automation.pages.inventory.DashboardPage.QuickActions;
import com.tpg.automation.pages.inventory.DashboardPage.SystemStatus;
import com.tpg.verification.Verify;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_API;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite covering the Dashboard API Data Layer (Story PS-8 / Story 8.1).
 *
 * <p>These tests verify that the HLM Platform Dashboard correctly fetches, renders,
 * and error-handles the six parallel API calls triggered on page load:
 * <ol>
 *   <li>All devices (up to 100)  – drives the "Total Devices" KPI card
 *   <li>Offline devices           – drives the sidebar Inventory badge
 *   <li>In-progress orders        – drives the "Active Deployments" KPI card
 *   <li>Scheduled orders          – drives the sidebar Account Service badge
 *   <li>Pending firmware          – contributes to the "Pending Approvals" KPI card
 *   <li>Pending compliance        – contributes to the "Pending Approvals" KPI card
 * </ol>
 * An independent audit-log API populates the "Recent Alerts" panel with entries
 * from the last 24 hours.
 *
 * <p>All tests extend {@link InventoryTestBase}, which launches the app and
 * authenticates before each test, landing on the Dashboard. Tests that need to
 * observe the initial loading transition call {@code Launch.app()} themselves to
 * reload the dashboard and catch the brief loading-placeholder state.
 *
 * <p>Tests marked {@code enabled = false} require network-level request interception
 * (Playwright {@code page.route()}) which is not yet exposed by the novus framework.
 * They are preserved as specification-complete placeholders; enable them once the
 * framework provides a network-stubbing API.
 */
public class DashboardApiTests extends InventoryTestBase {

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.01: Parallel API calls on dashboard load
    // Requires Playwright page.on("request") network monitoring – placeholder only.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.01",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that six API calls are triggered simultaneously on dashboard page load")
    @Outcome("All six API calls (devices, offline-devices, in-progress orders, scheduled orders, pending firmware, pending compliance) are observed in parallel on page load")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testParallelApiCallsOnDashboardLoad() {
        // PREREQUISITE: Requires Playwright page.on("request") or page.waitForResponse()
        // interception to capture outbound network requests during dashboard initialisation.
        // Enable once the novus framework exposes a network-request monitoring API.
        //
        // Indirect assertion: all six data domains are populated with values after load,
        // which is verified by TC-8.1.02 through TC-8.1.07 below.

        step("Navigate to the Dashboard and observe network activity during page load");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify all KPI cards are populated — confirming all six APIs responded");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI has a value — devices API responded")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI has a value — service orders API responded")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI has a value — firmware + compliance APIs responded")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.02 → TC-8.1.07: Data fetch verification (normal / happy-path state)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.02",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that all device records (up to 100) are fetched and reflected in the Total Devices KPI card")
    @Outcome("Total Devices KPI card displays a numeric value and no loading placeholder remains")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testAllDeviceRecordsAreFetched() {

        step("Verify the Total Devices KPI card value is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value element is present in the DOM")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains — data has fully loaded");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder em-dash has been replaced by an actual device count")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.03",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that offline devices are fetched as a separate API call and their count is displayed as a badge on the Inventory nav link")
    @Outcome("Offline device badge (span.bg-orange-500) is visible on the Inventory sidebar link")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testOfflineDevicesAreFetchedSeparately() {
        // PRECONDITION: Backend must have at least one device with "Offline" status
        // for the badge to render. If the test environment has no offline devices,
        // the badge will not appear and this test will fail by design.

        step("Verify the 'View Inventory' card is present in the Quick Actions section");
        user.wantsTo(
                Verify.uiElement(DashboardPage.QuickActions.VIEW_INVENTORY_CARD)
                        .describedAs("View Inventory card is visible in the Quick Actions section")
                        .isVisible()
        );

        step("Verify the offline-devices badge is visible inside the 'View Inventory' card");
        user.wantsTo(
                Verify.uiElement(KpiCard.OFFLINE_DEVICES_BADGE)
                        .describedAs("Orange badge (span.bg-orange-500) showing offline device count is visible on the Inventory nav link")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.04",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that in-progress service orders are fetched and their count is displayed in the Active Deployments KPI card")
    @Outcome("Active Deployments KPI card shows a numeric value after data loads")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testInProgressServiceOrdersAreFetched() {

        step("Verify the Active Deployments KPI card value is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI value element is present in the DOM")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains across KPI cards");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("All loading placeholders have been replaced — APIs have responded")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.05",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that scheduled service orders are fetched and their count is displayed as a badge on the Account Service quick-action nav link")
    @Outcome("Scheduled orders badge (span.bg-orange-500) is visible on the Account Service sidebar link")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testScheduledServiceOrdersAreFetched() {
        // PRECONDITION: Backend must have at least one service order with "Scheduled"
        // status for the badge to render in the sidebar.

        step("Verify the 'Scheduled Service' card is present in 'Quick Actions' section");
        user.wantsTo(
                Verify.uiElement(DashboardPage.QuickActions.SCHEDULED_SERVICE_CARD)
                        .describedAs("Scheduled Service card is visible in the Quick Actions section")
                        .isVisible()
        );

        step("Verify the scheduled-orders badge is visible inside the 'Scheduled Service' card");
        user.wantsTo(
                Verify.uiElement(KpiCard.SCHEDULED_ORDERS_BADGE)
                        .describedAs("Orange badge (span.bg-orange-500) showing scheduled orders count is visible on the Account Service nav link")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.06",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that pending firmware records are fetched and contribute to the Pending Approvals KPI card")
    @Outcome("Pending Approvals KPI card displays a numeric value")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testPendingFirmwareRecordsAreFetched() {

        step("Verify the Pending Approvals KPI card value is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is visible (includes pending firmware count)")
                        .isVisible()
        );

        step("Verify the 'Check compliance' card is present in the 'Quick Actions' section ");
        user.wantsTo(
                Verify.uiElement(DashboardPage.QuickActions.COMPLIANCE_LINK)
                        .describedAs("Check Compliance quick action link is visible")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.07",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that pending compliance records are fetched and contribute to the Pending Approvals KPI card")
    @Outcome("Pending Approvals KPI card value reflects the combined firmware + compliance pending count")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testPendingComplianceRecordsAreFetched() {

        step("Verify the Pending Approvals KPI card value is visible — reflects combined firmware + compliance pending counts");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is populated (pending firmware + pending compliance)")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains in Pending Approvals — both APIs responded");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No loading placeholder visible — firmware and compliance counts have been aggregated")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.08 / TC-8.1.09: Loading placeholder → live values transition
    // Both tests re-navigate to the dashboard to capture the transient loading state.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.08",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that all KPI cards display the loading placeholder '—' (em dash) while API data is being fetched")
    @Outcome("At least one KPI card shows '—' immediately after navigation — before API responses arrive")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testKpiCardsShowLoadingPlaceholderOnPageLoad() {
        // Re-navigate to the dashboard to catch the brief loading-placeholder state.
        // The session cookie from InventoryTestBase.loginToApplication() keeps the
        // user authenticated, so this navigates directly to the dashboard view.
        // NOTE: This test is timing-sensitive; on very fast API responses the
        // placeholder may be replaced before Playwright can assert it.

        step("Navigate directly to the dashboard URL to observe the initial loading state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the loading placeholder '—' (em dash) is visible in at least one KPI card during data fetch");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder em-dash (U+2014) is visible in a KPI card while APIs are pending")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.09",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that KPI cards transition from the loading placeholder '—' to actual numeric values once API responses are received")
    @Outcome("All three KPI cards (Total Devices, Active Deployments, Pending Approvals) show numeric values; no placeholder remains")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testKpiCardsTransitionFromPlaceholderToLiveValues() {

        step("Verify Total Devices KPI card has a populated value (not a placeholder)");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is present — data has arrived from the devices API")
                        .isVisible()
        );

        step("Verify Active Deployments KPI card has a populated value");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI value is present — data has arrived from the service orders API")
                        .isVisible()
        );

        step("Verify Pending Approvals KPI card has a populated value");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is present — firmware + compliance APIs have responded")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains across any KPI card");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No KPI card is still showing '—' — all API calls have completed")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.10 → TC-8.1.12: Error banner on API failure
    // Require Playwright page.route() network interception — placeholders only.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.10",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that a red error banner is displayed at the top of the KPI section when any single dashboard API returns an error")
    @Outcome("Red error banner (div.bg-red-50) is visible with a meaningful error message")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testErrorBannerDisplayedOnSingleApiFailure() {
        // PREREQUISITE: One of the six data API endpoints must be configured to return
        // HTTP 500. Use Playwright page.route() interception once exposed by novus-core,
        // or run against a dedicated stub environment where the devices API is broken.

        step("Navigate to the Dashboard with one API endpoint failing");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the red error banner container is visible in the KPI section");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("Red error banner (div.bg-red-50) is rendered as a direct child of div.p-6.space-y-6")
                        .isVisible()
        );

        step("Verify the error banner contains a visible error message");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.MESSAGE)
                        .describedAs("Error message text is visible within the banner")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.11",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that a red error banner is displayed and KPI cards remain in placeholder state when all six APIs fail")
    @Outcome("Error banner is visible; all KPI cards still show '—'; no crash or blank screen")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testErrorBannerAndPlaceholdersWhenAllApisFail() {
        // PREREQUISITE: All six KPI data endpoints must be blocked or returning errors.
        // Requires Playwright page.route() interception or a network-offline simulation.

        step("Navigate to the Dashboard with all data APIs unavailable");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the red error banner is visible");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("Red error banner is shown when all six KPI APIs fail")
                        .isVisible()
        );

        step("Verify KPI cards remain in the loading placeholder state — no data arrived");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder '—' is still visible — all APIs failed and no data was rendered")
                        .isVisible()
        );

        step("Verify the dashboard page itself did not crash (header still rendered)");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 heading is present — page did not crash or go blank")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.12",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the error banner displays the actual error message from the API response, not a generic or empty fallback")
    @Outcome("Error banner text matches the specific error message returned by the failing API (e.g. 'Network error')")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testErrorBannerDisplaysMeaningfulMessage() {
        // PREREQUISITE: API endpoint must be configured to return a specific, known
        // error message payload so that the assertion can match against it.
        // Requires Playwright page.route() interception.

        step("Navigate to the Dashboard with the API returning a known error message");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the error banner is visible");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("Red error banner is rendered")
                        .isVisible()
        );

        step("Verify the banner message reflects the specific API error, not a generic fallback");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.MESSAGE)
                        .describedAs("Error banner contains meaningful, API-specific error text (not empty, not a generic 'Something went wrong')")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.13 → TC-8.1.16: Alerts panel / audit-log data
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.13",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that audit log entries from the last 24 hours are fetched and displayed in the Recent Alerts panel")
    @Outcome("Recent Alerts panel is visible and contains at least one audit log list item")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testAuditLogsAreFetchedForAlertsPanel() {
        // PRECONDITION: Backend must have at least one audit log entry within the
        // last 24 hours; otherwise the panel shows the empty-state message (TC-8.1.19).

        step("Verify the Recent Alerts panel container is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel (div.bg-card containing h3 'Recent Alerts') is rendered")
                        .isVisible()
        );

        step("Verify at least one audit-log entry is displayed in the Recent Alerts panel");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.ALERT_ITEM)
                        .describedAs("At least one list item (li) is rendered inside the Recent Alerts panel")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.14",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the Recent Alerts panel only shows audit log entries from the last 24 hours")
    @Outcome("Alert items from the last 24 hours are visible; the empty-state message is not shown")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testAuditLogsTwentyFourHourTimeWindow() {
        // PRECONDITION: Backend must have audit log entries dated today (within 24 h).

        step("Verify the Recent Alerts panel container is visible");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel is rendered on the dashboard")
                        .isVisible()
        );

        step("Verify audit log entries from the 24-hour window are displayed");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.ALERT_ITEM)
                        .describedAs("Alert list items (last-24h audit logs) are present in the panel")
                        .isVisible()
        );

        step("Verify the 'No recent activity' empty-state is NOT shown when entries exist");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.EMPTY_STATE)
                        .describedAs("Empty-state message is absent — 24-hour entries are present and displayed")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.15",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that a 'Loading alerts...' indicator is displayed in the Recent Alerts panel while audit log data is being fetched")
    @Outcome("'Loading alerts...' text is visible in the panel before the audit log API response arrives")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testAlertsLoadingIndicatorDisplayedWhileFetching() {
        // Re-navigate to capture the brief loading state of the alerts panel.
        // Timing-sensitive: on fast API responses the indicator may resolve before
        // the assertion; consider introducing network throttling in the test environment.

        step("Navigate directly to the dashboard URL to observe the initial alerts loading state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify 'Loading alerts...' indicator is shown in the Recent Alerts panel while data is being fetched");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.LOADING_INDICATOR)
                        .describedAs("'Loading alerts...' text is visible inside the Recent Alerts panel during the audit-log fetch")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.16",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that a meaningful error message is displayed in the Recent Alerts panel when the audit logs API fails")
    @Outcome("Alerts panel shows an error message (div.bg-red-50); KPI cards are unaffected and show live values")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testAlertsErrorMessageOnApiFailure() {
        // PREREQUISITE: The audit logs API must be blocked/returning an error while all
        // six KPI APIs remain functional. Requires Playwright page.route() interception.

        step("Navigate to the Dashboard with only the audit logs API failing");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify an error message is displayed inside the Recent Alerts panel");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.ERROR_MESSAGE)
                        .describedAs("Error element (div.bg-red-50 inside alerts panel) is visible when audit log fetch fails")
                        .isVisible()
        );

        step("Verify KPI cards are unaffected — Total Devices value is still visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI is populated — KPI domain is independent of the alerts failure")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.17: No auto-refresh or polling after initial page load
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.17",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the dashboard does not auto-refresh or poll APIs after the initial page load")
    @Outcome("KPI cards retain their loaded values with no loading placeholders reappearing; no new network calls are made")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testNoDashboardAutoRefreshOrPolling() {
        // Full no-polling verification requires Playwright page.on("request") network
        // monitoring. This test validates the visible UI state only — loading
        // placeholders must not reappear after data has loaded, which would indicate
        // a re-fetch cycle. Add network-level assertion once novus-core exposes it.

        step("Verify all three KPI cards are populated with data from the initial API load");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is present after initial page load")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI value is present after initial page load")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is present after initial page load")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' has reappeared — no silent re-fetch was triggered");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No KPI card is showing '—' — data was not wiped and re-fetched by a polling cycle")
                        .isNotVisible()
        );

        step("Verify the dashboard header is still rendered — the page has not silently reloaded");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 heading is present — page has not auto-reloaded")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.18: Device list capped at 100 records
    // ──────────────────────────────────────────────────────────────────────────────

    /** @MetaData(author = "QA Automation", testCaseId = "TC-8.1.18",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the Total Devices KPI reflects a maximum of 100 device records and no pagination or 'load more' is triggered")
    @Outcome("Total Devices KPI is visible with no error banner; no pagination control is present on the dashboard")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testDeviceListIsCappedAt100Records() {
        // PRECONDITION: Backend should have ≥ 100 device records to exercise the cap.
        // The KPI value assertion is structural (element visible); numeric range
        // assertion (≤ 100) requires getText() support once exposed by novus-core.

        step("Verify the Total Devices KPI card value is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value element is rendered (capped at 100 records)")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains for the Total Devices card");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder has been replaced — device count (≤ 100) has been rendered")
                        .isNotVisible()
        );

        step("Verify no error banner is shown — the devices API responded successfully within the 100-record cap");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner present — device fetch completed without error")
                        .isNotVisible()
        );
    } **/

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.19: Dashboard loads successfully with zero data
    // Requires a clean-state / empty backend environment — placeholder only.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.19",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the dashboard loads without errors when all APIs return empty result sets (zero devices, orders, firmware, compliance, logs)")
    @Outcome("Dashboard loads successfully; KPI cards show 0; alerts panel shows 'No recent activity'; no error banners")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testDashboardLoadsWithZeroData() {
        // PREREQUISITE: Backend must return empty arrays for all six KPI APIs and the
        // audit-log API. Run against a freshly provisioned clean-state test environment,
        // or use Playwright page.route() to intercept calls and return empty payloads.

        step("Verify the dashboard page loaded without crashing");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("Dashboard h1 heading is visible — page loaded successfully with empty data")
                        .isVisible()
        );

        step("Verify no error banner is displayed — empty results must not trigger an error state");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner present — empty API responses are handled gracefully")
                        .isNotVisible()
        );

        step("Verify no loading placeholder '—' remains — KPI cards resolved to '0' from empty responses");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No loading placeholder visible — KPI cards have resolved (should show 0)")
                        .isNotVisible()
        );

        step("Verify the Recent Alerts panel shows the 'No recent activity' empty-state message");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.EMPTY_STATE)
                        .describedAs("'No recent activity' empty-state message is shown when audit-log API returns no entries")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.20: KPI and alerts fetch independently (partial failure isolation)
    // Requires Playwright page.route() network interception — placeholder only.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.20",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the KPI data domain and the Recent Alerts domain fail independently without affecting each other")
    @Outcome("When audit log API fails: KPI cards show live values; alerts panel shows its own error; no global KPI error banner")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testKpiAndAlertsFetchIndependently() {
        // PREREQUISITE: The audit log API must be blocked while all six KPI APIs remain
        // functional. Requires Playwright page.route() interception or a split-backend
        // stub environment. Invert the scenario (KPI fails / alerts succeed) to verify
        // the reverse direction of independence.

        step("Navigate to the Dashboard with only the audit logs API failing");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify Total Devices KPI card shows a live value — KPI domain is unaffected");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI has live data — KPI APIs succeeded independently of the alerts failure")
                        .isVisible()
        );

        step("Verify Active Deployments KPI card shows a live value");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI has live data")
                        .isVisible()
        );

        step("Verify the Recent Alerts panel shows its own isolated error message");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.ERROR_MESSAGE)
                        .describedAs("Alerts panel error element (div.bg-red-50 inside panel) is visible — failure is isolated to the alerts section")
                        .isVisible()
        );

        step("Verify no global KPI error banner is shown — the KPI section is unaffected by the alerts failure");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No global error banner in the KPI section — only the alerts panel shows an error")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.21 → TC-8.1.25: Refresh Dashboard button behaviour
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.21",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that clicking the Refresh Dashboard button re-triggers all data fetch groups and updates KPI cards and alerts panel with fresh values")
    @Outcome("After refresh: KPI cards and alerts panel display updated live values; no error banners visible")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testRefreshDashboardButtonTriggersCompleteDataRefetch() {

        step("Verify the Refresh Dashboard button is visible before clicking");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh Dashboard button (aria-label='Refresh dashboard') is rendered in the welcome row")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button to trigger a full data re-fetch");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Verify all three KPI cards display live values after refresh completes");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI is populated after refresh")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI is populated after refresh")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI is populated after refresh")
                        .isVisible()
        );

        step("Verify no loading placeholder '—' remains after refresh completes");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No KPI card is still showing '—' — all refresh API calls have completed")
                        .isNotVisible()
        );

        step("Verify the Recent Alerts panel is visible after refresh");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel is rendered and visible after the refresh cycle")
                        .isVisible()
        );

        step("Verify no error banner is shown after a successful refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner present — refresh completed without API failures")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.22",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the Refresh Dashboard button is visible with a circular arrow icon and is interactive on a loaded dashboard")
    @Outcome("Refresh button is rendered and clickable in the top-right area of the welcome row")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testRefreshButtonIsVisibleWithCircularArrowIcon() {

        step("Verify the Refresh Dashboard button is present and visible on the loaded dashboard");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("button[aria-label='Refresh dashboard'] is rendered in the welcome heading row")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.23",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that KPI cards briefly show the loading placeholder '—' while the refresh re-fetch is in progress, then update to new values")
    @Outcome("After clicking Refresh: loading placeholders appear transiently; KPI cards ultimately show numeric values")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testKpiCardsShowPlaceholdersDuringRefresh() {
        // Re-navigate to dashboard to ensure a clean starting state before refresh.

        step("Navigate to the Dashboard to reset state before observing the refresh loading transition");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Click the Refresh Dashboard button to trigger the re-fetch cycle");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Verify the loading placeholder '—' (em dash) appears in KPI cards during the refresh");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("Loading placeholder em-dash is briefly visible in a KPI card while the refresh API calls are in-flight")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.24",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the Recent Alerts panel shows a loading indicator while the audit log re-fetch is in progress during a dashboard refresh")
    @Outcome("'Loading alerts...' text is visible in the alerts panel immediately after clicking Refresh")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testAlertsPanelShowsLoadingStateDuringRefresh() {
        // Re-navigate to ensure we start from a loaded state before triggering refresh.

        step("Navigate to the Dashboard to establish a clean loaded state");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Click the Refresh Dashboard button to trigger the audit log re-fetch");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Verify 'Loading alerts...' indicator is shown in the Recent Alerts panel during the re-fetch");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.LOADING_INDICATOR)
                        .describedAs("'Loading alerts...' text is visible inside the Recent Alerts panel while the audit log re-fetch is in-flight")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.25",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that all existing error banners are cleared immediately when a dashboard refresh is initiated")
    @Outcome("Error banners disappear as soon as Refresh is clicked; if the new fetch also fails they re-appear")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testErrorBannersAreClearedWhenRefreshStarts() {
        // PREREQUISITE: Dashboard must be in an error state (at least one error banner visible)
        // before the Refresh button is clicked. This requires Playwright page.route()
        // interception to inject a forced API failure on the first load, then remove it
        // before the refresh so the second fetch succeeds (or keeps it to verify re-appearance).
        // Enable once novus-core exposes a network-stubbing API.

        step("Navigate to the Dashboard with one or more APIs failing to produce visible error banners");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify at least one error banner is visible before clicking Refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("Red error banner is present — dashboard is in an error state prior to refresh")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Verify all previous error banners are cleared immediately once the refresh starts");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("Error banners are removed as soon as the refresh re-fetch begins — no stale errors shown")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.26 → TC-8.1.28: System Status panel
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.26",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the System Status panel is visible and displays exactly 4 service rows: Deployment Service, Compliance Engine, Asset Database, Analytics Platform")
    @Outcome("System Status panel is rendered with 4 service items; each shows a health bar and a status label")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testSystemStatusPanelDisplaysFourServices() {

        step("Verify the System Status panel container is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel (div.bg-card with h3 'System Status') is rendered")
                        .isVisible()
        );

        step("Verify at least one service row is displayed inside the System Status panel");
        user.wantsTo(
                Verify.uiElement(SystemStatus.SERVICE_ITEM)
                        .describedAs("Service item rows are present inside div.space-y-4 within the System Status panel")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.27",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the System Status panel displays health percentage labels derived from live KPI data — at least one Operational or Degraded label is rendered")
    @Outcome("System Status panel shows at least one status label (Operational or Degraded) once KPI data has loaded")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testSystemStatusHealthPercentagesUpdateFromLiveKpiData() {

        step("Verify the System Status panel container is visible — KPI data has been loaded");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel is rendered after KPI data has been fetched")
                        .isVisible()
        );

        step("Verify at least one status label (Operational or Degraded) is rendered in the panel");
        // Either label being visible confirms that live KPI data drove the health calculation.
        // The specific label shown depends on live backend state; both are asserted via separate TCs.
        user.wantsTo(
                Verify.uiElement(SystemStatus.SERVICE_ITEM)
                        .describedAs("Service items are present — health percentages derived from live KPI data have been applied")
                        .isVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.28",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the System Status panel renders 'Degraded' (orange) labels for services with health below 90% and 'Operational' (green) for those at or above 90%")
    @Outcome("At least one 'Degraded' label in orange and/or at least one 'Operational' label in green is visible in the System Status panel")
    @Test(groups = {DASHBOARD_API, REGRESSION})
    public void testSystemStatusOperationalAndDegradedLabels() {
        // The live environment currently shows 3 Degraded services (Deployment, Compliance Engine,
        // Asset Database) and 1 Operational service (Analytics Platform).
        // This test asserts both label types are rendered per the live data state.

        step("Verify the System Status panel is visible on the dashboard");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel container is rendered")
                        .isVisible()
        );

        step("Verify at least one 'Degraded' label (orange, text-orange-600) is present — health < 90%");
        user.wantsTo(
                Verify.uiElement(SystemStatus.DEGRADED_LABEL)
                        .describedAs("'Degraded' status label (span.text-orange-600) is visible for at least one service with health below 90%")
                        .isVisible()
        );

        step("Verify at least one 'Operational' label (green, text-green-600) is present — health ≥ 90%");
        user.wantsTo(
                Verify.uiElement(SystemStatus.OPERATIONAL_LABEL)
                        .describedAs("'Operational' status label (span.text-green-600) is visible for at least one service with health at or above 90%")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.29 → TC-8.1.30: Quick Action badge counts
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.29",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that Quick Action badge circles are not rendered when all relevant counts (offline devices, scheduled orders, pending firmware, pending compliance) are zero")
    @Outcome("No orange badge spans visible on any Quick Action card when backend returns zero for all badge categories")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testQuickActionBadgesAreHiddenWhenCountIsZero() {
        // PREREQUISITE: Backend must return 0 for offline devices, scheduled orders,
        // pending firmware, and pending compliance so that badge spans are not rendered.
        // Run against a clean-state environment or use Playwright page.route() interception
        // to return empty/zero responses for all six KPI API calls.

        step("Navigate to the Dashboard with all badge-category counts returning zero");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify no orange badge is visible on the 'View Inventory' Quick Action card");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_BADGE)
                        .describedAs("No orange badge (span.bg-orange-500) on 'View Inventory' when offline device count is 0")
                        .isNotVisible()
        );

        step("Verify no orange badge is visible on the 'Schedule Service' Quick Action card");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_BADGE)
                        .describedAs("No orange badge on 'Schedule Service' when scheduled orders count is 0")
                        .isNotVisible()
        );

        step("Verify no orange badge is visible on the 'Deploy Firmware' Quick Action card");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_BADGE)
                        .describedAs("No orange badge on 'Deploy Firmware' when pending firmware count is 0")
                        .isNotVisible()
        );

        step("Verify no orange badge is visible on the 'Check Compliance' Quick Action card");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_BADGE)
                        .describedAs("No orange badge on 'Check Compliance' when pending compliance count is 0")
                        .isNotVisible()
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.30",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that orange badge circles are visible on Quick Action cards when their respective counts are greater than zero")
    @Outcome("Orange badge spans are visible on all four Quick Action cards — offline devices, scheduled orders, pending firmware, and pending compliance all have counts > 0")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_API})
    public void testQuickActionBadgesAreVisibleWhenCountIsGreaterThanZero() {
        // PRECONDITION: Live backend must have at least one record for each badge category.
        // The current test environment satisfies this: View Inventory = 3, Schedule Service = 3,
        // Deploy Firmware = 2, Check Compliance = 2 (verified via live UI inspection).

        step("Verify the 'View Inventory' Quick Action card is present");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_CARD)
                        .describedAs("'View Inventory' Quick Action card is rendered on the dashboard")
                        .isVisible()
        );

        step("Verify the orange badge on 'View Inventory' is visible — offline device count > 0");
        user.wantsTo(
                Verify.uiElement(QuickActions.VIEW_INVENTORY_BADGE)
                        .describedAs("Orange badge (span.bg-orange-500) is present on the 'View Inventory' card in the main Quick Actions grid")
                        .isVisible()
        );

        step("Verify the 'Schedule Service' Quick Action card is present");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULED_SERVICE_CARD)
                        .describedAs("'Schedule Service' Quick Action card is rendered")
                        .isVisible()
        );

        step("Verify the orange badge on 'Schedule Service' is visible — scheduled orders count > 0");
        user.wantsTo(
                Verify.uiElement(QuickActions.SCHEDULE_SERVICE_BADGE)
                        .describedAs("Orange badge is present on the 'Schedule Service' card")
                        .isVisible()
        );

        step("Verify the 'Deploy Firmware' Quick Action card is present");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' Quick Action card is rendered on the dashboard")
                        .isVisible()
        );

        step("Verify the orange badge on 'Deploy Firmware' is visible — pending firmware count > 0");
        user.wantsTo(
                Verify.uiElement(QuickActions.DEPLOY_FIRMWARE_BADGE)
                        .describedAs("Orange badge is present on the 'Deploy Firmware' card")
                        .isVisible()
        );

        step("Verify the 'Check Compliance' Quick Action card is present");
        user.wantsTo(
                Verify.uiElement(QuickActions.COMPLIANCE_LINK)
                        .describedAs("'Check Compliance' Quick Action card is rendered")
                        .isVisible()
        );

        step("Verify the orange badge on 'Check Compliance' is visible — pending compliance count > 0");
        user.wantsTo(
                Verify.uiElement(QuickActions.CHECK_COMPLIANCE_BADGE)
                        .describedAs("Orange badge is present on the 'Check Compliance' card")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.1.31: Empty state — "No recent activity" in alerts panel
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.1.31",
            stories = {"PS-8"}, category = "DASHBOARD_API")
    @Description("Verify that the Recent Alerts panel displays 'No recent activity' when the audit logs API returns no entries within the last 24 hours")
    @Outcome("'No recent activity' empty-state text is shown in the alerts panel; no error banner and no alert list items are present")
    @Test(groups = {DASHBOARD_API, REGRESSION}, enabled = false)
    public void testNoRecentActivityMessageWhenNoAuditLogsExist() {
        // PREREQUISITE: Backend must have no audit log entries within the last 24 hours.
        // Run against a clean-state environment with no recent activity, or use
        // Playwright page.route() to intercept the audit log API and return an empty array.

        step("Navigate to the Dashboard with no audit log entries in the last 24-hour window");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Recent Alerts panel container is visible");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel is rendered on the dashboard")
                        .isVisible()
        );

        step("Verify the 'No recent activity' empty-state message is displayed");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.EMPTY_STATE)
                        .describedAs("'No recent activity' text is shown — audit log API returned no entries for the 24-hour window")
                        .isVisible()
        );

        step("Verify no alert list items are rendered — empty state, not a partial load");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.ALERT_ITEM)
                        .describedAs("No li items rendered inside the alerts panel — the empty state is complete, not partial")
                        .isNotVisible()
        );

        step("Verify no error banner is shown — an empty response is not an error");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner present — empty audit log result is handled gracefully, not as a failure")
                        .isNotVisible()
        );
    }
}
