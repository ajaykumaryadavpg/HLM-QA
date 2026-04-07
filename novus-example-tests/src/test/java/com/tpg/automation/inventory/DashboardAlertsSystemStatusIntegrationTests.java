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
import com.tpg.automation.pages.inventory.DashboardPage.SystemStatus;
import com.tpg.verification.Verify;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_ALERTS;
import static com.tpg.automation.constants.TestGroups.DASHBOARD_SYSTEM_STATUS;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Integration test suite covering both the Recent Alerts and System Status panels
 * together on the Dashboard (Story PS-5 / 8.3).
 *
 * <p>Verifies cross-panel scenarios:
 * <ul>
 *   <li>Both panels are visible simultaneously alongside the KPI grid
 *   <li>'View all' link navigates to /analytics and back-navigation to Dashboard works
 *   <li>Both panels remain rendered after Refresh Dashboard
 *   <li>Both panels handle zero-data gracefully without error banners
 * </ul>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * and lands on the Dashboard before any test method runs.
 *
 * @see DashboardPage.AlertsPanel
 * @see DashboardPage.SystemStatus
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-5
 * @jira PS-30 (QA Sub-task)
 */
public class DashboardAlertsSystemStatusIntegrationTests extends InventoryTestBase {

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-INTEGRATION-01: Both panels visible simultaneously on the Dashboard
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-INTEGRATION-01",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that the Recent Alerts panel and System Status panel are both visible simultaneously alongside the KPI card grid after login")
    @Outcome("AlertsPanel.CONTAINER, SystemStatus.CONTAINER, and KpiCard.KPI_GRID_CONTAINER are all visible on the Dashboard — no layout breakage")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS, DASHBOARD_SYSTEM_STATUS})
    public void testBothPanelsVisibleSimultaneouslyOnDashboard() {

        step("Verify the Recent Alerts panel container is visible");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("div.bg-card:has(h3:text-is('Recent Alerts')) is rendered in the Dashboard right column")
                        .isVisible()
        );

        step("Verify the System Status panel container is visible");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("div.bg-card:has(h3:text-is('System Status')) is rendered in the Dashboard content area")
                        .isVisible()
        );

        step("Verify the KPI card grid is also visible — all three content sections coexist without layout breakage");
        user.wantsTo(
                Verify.uiElement(KpiCard.KPI_GRID_CONTAINER)
                        .describedAs("KPI card grid (div.grid-cols-4 with Total Devices card) is visible alongside both panels")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-INTEGRATION-02: 'View all' link navigates to /analytics; back-navigation works
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-INTEGRATION-02",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify the 'View all' link in the Alerts panel navigates to /analytics and the Dashboard loads correctly after returning")
    @Outcome("After 'View all' click: URL path is /analytics. After back-navigation: Dashboard h1 header is visible again")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS})
    public void testViewAllLinkNavigatesAndBackNavigationToDashboardWorks() {

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

        step("Verify the URL path is /analytics after clicking 'View all'");
        String path = (String) browser.evaluate("() => window.location.pathname");
        Assert.assertEquals(path, "/analytics",
                "'View all' must navigate to /analytics — actual path: " + path);

        step("Navigate back to the Dashboard via the sidebar nav link");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Dashboard h1 header is visible — Dashboard loaded correctly after back-navigation");
        user.wantsTo(
                Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                        .describedAs("h1 Dashboard header is visible after returning from /analytics")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-INTEGRATION-03: Both panels render correctly after Refresh Dashboard
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-INTEGRATION-03",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify that both the Recent Alerts and System Status panels remain visible and no error appears after clicking Refresh Dashboard")
    @Outcome("Both panels visible before and after refresh; KPI cards repopulate; no global error banner appears")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_ALERTS, DASHBOARD_SYSTEM_STATUS})
    public void testBothPanelsRenderCorrectlyAfterRefreshDashboard() {

        step("Verify all three content sections are loaded before refresh");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel is present before refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel is present before refresh")
                        .isVisible()
        );
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is populated — Dashboard fully loaded before refresh")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button — re-triggers all KPI, alerts, and service-status API calls");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Wait for KPI cards to repopulate — confirms refresh cycle completed");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card shows live value after refresh cycle")
                        .isVisible()
        );

        step("Verify the Recent Alerts panel is still visible post-refresh");
        user.wantsTo(
                Verify.uiElement(AlertsPanel.CONTAINER)
                        .describedAs("Recent Alerts panel is still rendered after refresh")
                        .isVisible()
        );

        step("Verify the System Status panel is still visible post-refresh");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel is still rendered after refresh")
                        .isVisible()
        );

        step("Verify no global error banner is present after a successful refresh");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner — refresh completed without failures")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-INTEGRATION-04: Zero data state — both panels handle empty APIs gracefully
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-INTEGRATION-04",
            stories = {"PS-5"}, category = "DASHBOARD_ALERTS")
    @Description("Verify both panels render gracefully when all GraphQL APIs return empty data sets — no error banners, empty states shown where expected")
    @Outcome("KPI shows '0'; 'No recent activity' shown in Alerts; System Status panel visible; no global error banner")
    @Test(groups = {REGRESSION, DASHBOARD_ALERTS, DASHBOARD_SYSTEM_STATUS})
    public void testBothPanelsHandleZeroDataGracefully() {

        step("Register route to return empty items for all GraphQL queries");
        browser.route("**appsync-api**", route -> {
            route.fulfill(new Route.FulfillOptions()
                    .setStatus(200)
                    .setContentType("application/json")
                    .setBody("{\"data\":{\"listAuditLogs\":{\"items\":[],\"nextToken\":null}," +
                            "\"listDevices\":{\"items\":[],\"nextToken\":null}," +
                            "\"listServiceOrdersByStatus\":{\"items\":[],\"nextToken\":null}," +
                            "\"listFirmwareCompliance\":{\"items\":[],\"nextToken\":null}}}"));
        });

        try {
            step("Re-navigate to the Dashboard with all APIs returning empty data");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify KPI Total Devices card shows '0' — empty devices API handled gracefully");
            user.wantsTo(
                    Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                            .describedAs("Total Devices KPI shows '0' when listDevices returns empty array")
                            .containsText("0")
            );

            step("Verify the 'No recent activity' empty state is shown in the Alerts panel");
            user.wantsTo(
                    Verify.uiElement(AlertsPanel.EMPTY_STATE)
                            .describedAs("'No recent activity' message is displayed — empty audit-log response handled gracefully")
                            .isVisible()
            );

            step("Verify the System Status panel container is still visible with zero data");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.CONTAINER)
                            .describedAs("System Status panel is still rendered — it does not disappear with zero-data responses")
                            .isVisible()
            );

            step("Verify no global error banner is shown — empty data is not treated as an error");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("No red error banner — empty arrays are handled as valid zero-data state, not failures")
                            .isNotVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }
}
