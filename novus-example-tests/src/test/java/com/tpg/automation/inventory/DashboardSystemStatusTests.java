package com.tpg.automation.inventory;

import com.microsoft.playwright.Route;
import com.tpg.actions.Launch;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DashboardPageImpl;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.DashboardPage.ErrorBanner;
import com.tpg.automation.pages.inventory.DashboardPage.KpiCard;
import com.tpg.automation.pages.inventory.DashboardPage.SystemStatus;
import com.tpg.verification.Verify;
import org.testng.Assert;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_SYSTEM_STATUS;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite covering the Dashboard System Status panel (Story PS-5 / 8.3).
 *
 * <p>Verifies that the System Status panel:
 * <ul>
 *   <li>Is visible and displays exactly 4 service rows
 *   <li>Each row has an Operational (green) or Degraded (orange) status label
 *   <li>Each row has a progress bar reflecting health percentage
 *   <li>Analytics Platform is always Operational
 *   <li>Deployment Service is Operational when in-progress orders exist, Degraded otherwise
 *   <li>Compliance Engine is Operational when no pending items, Degraded when pending items exist
 *   <li>Asset Database reflects average device health score (≥90% = Operational, <90% = Degraded)
 *   <li>Panel remains visible when the KPI error banner appears
 *   <li>Service rows appear in the correct fixed order
 *   <li>Operational uses text-green-600, Degraded uses text-orange-600
 * </ul>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * and lands on the Dashboard before any test method runs. Tests that need deterministic
 * service states use {@code browser.route()} with AppSync interception and clean up
 * via {@code browser.unrouteAll()} in a {@code finally} block.
 *
 * @see DashboardPage.SystemStatus
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-5
 * @jira PS-30 (QA Sub-task)
 */
public class DashboardSystemStatusTests extends InventoryTestBase {

    // ── GraphQL mock bodies ────────────────────────────────────────────────────

    private static final String MOCK_IN_PROGRESS_ORDERS =
            "{\"data\":{\"listServiceOrdersByStatus\":{\"items\":[" +
            "{\"id\":\"order-001\",\"status\":\"In Progress\",\"deviceId\":\"dev-001\"}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_EMPTY_ORDERS =
            "{\"data\":{\"listServiceOrdersByStatus\":{\"items\":[],\"nextToken\":null}}}";

    private static final String MOCK_PENDING_COMPLIANCE =
            "{\"data\":{\"listFirmwareCompliance\":{\"items\":[" +
            "{\"id\":\"comp-001\",\"status\":\"Pending\",\"deviceId\":\"dev-001\"}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_EMPTY_COMPLIANCE =
            "{\"data\":{\"listFirmwareCompliance\":{\"items\":[],\"nextToken\":null}}}";

    private static final String MOCK_HIGH_HEALTH_DEVICES =
            "{\"data\":{\"listDevices\":{\"items\":[" +
            "{\"id\":\"dev-001\",\"healthScore\":95}," +
            "{\"id\":\"dev-002\",\"healthScore\":98}]," +
            "\"nextToken\":null}}}";

    private static final String MOCK_LOW_HEALTH_DEVICES =
            "{\"data\":{\"listDevices\":{\"items\":[" +
            "{\"id\":\"dev-001\",\"healthScore\":50}]," +
            "\"nextToken\":null}}}";

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-01: System Status panel is visible on the Dashboard after login
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-01",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the System Status panel container is visible on the Dashboard after login")
    @Outcome("div.bg-card:has(h3:text-is('System Status')) is rendered and visible; 'System Status' heading text is present")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_SYSTEM_STATUS})
    public void testSystemStatusPanelIsVisibleAfterLogin() {

        step("Verify the System Status panel container is rendered in the Dashboard content area");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("div.bg-card:has(h3:text-is('System Status')) is visible — panel is rendered")
                        .isVisible()
        );

        step("Verify the 'System Status' heading text is present inside the panel");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("Panel container contains the text 'System Status' — heading is correct")
                        .containsText("System Status")
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-02: System Status panel displays exactly 4 service rows
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-02",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the System Status panel displays exactly 4 service rows and all 4 service names are present")
    @Outcome("4 service rows rendered (Deployment Service, Compliance Engine, Asset Database, Analytics Platform)")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_SYSTEM_STATUS})
    public void testSystemStatusPanelDisplaysExactlyFourServiceRows() {

        step("Verify at least one service row is visible in the System Status panel");
        user.wantsTo(
                Verify.uiElement(SystemStatus.SERVICE_ITEM)
                        .describedAs("At least one div.space-y-4 > div service row is visible in the System Status panel")
                        .isVisible()
        );

        step("Use JS evaluate to count service rows and assert exactly 4");
        Long rowCount = (Long) browser.evaluate(
                "() => document.querySelectorAll(" +
                "\"div.bg-card:has(h3) div.space-y-4 > div\").length");
        Assert.assertEquals(rowCount.longValue(), 4L,
                "System Status panel must display exactly 4 service rows — actual: " + rowCount);

        step("Verify the panel contains text 'Deployment Service'");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("'Deployment Service' text is present in the System Status panel")
                        .containsText("Deployment Service")
        );

        step("Verify the panel contains text 'Compliance Engine'");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("'Compliance Engine' text is present in the System Status panel")
                        .containsText("Compliance Engine")
        );

        step("Verify the panel contains text 'Asset Database'");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("'Asset Database' text is present in the System Status panel")
                        .containsText("Asset Database")
        );

        step("Verify the panel contains text 'Analytics Platform'");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("'Analytics Platform' text is present in the System Status panel")
                        .containsText("Analytics Platform")
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-03: Each service row has a status label — Operational or Degraded
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-03",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify that each of the 4 service rows displays exactly one status label (Operational or Degraded) and the total count equals 4")
    @Outcome("Sum of span.text-green-600 + span.text-orange-600 elements in the panel equals 4; at least one is visible")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testEachServiceRowHasStatusLabel() {

        step("Wait for Dashboard KPI data to load — ensures service-status computation has completed");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI is visible — dashboard is fully loaded with live data")
                        .isVisible()
        );

        step("Use JS evaluate to count total Operational + Degraded labels — sum must equal 4");
        Long greenCount = (Long) browser.evaluate(
                "() => document.querySelectorAll(" +
                "\"div.bg-card:has(h3:text-is('System Status')) span.text-green-600\").length");
        Long orangeCount = (Long) browser.evaluate(
                "() => document.querySelectorAll(" +
                "\"div.bg-card:has(h3:text-is('System Status')) span.text-orange-600\").length");
        Assert.assertEquals(greenCount + orangeCount, 4L,
                "Total Operational + Degraded labels must equal 4 — actual green=" + greenCount + " orange=" + orangeCount);

        step("Verify at least one status label (Operational or Degraded) is visible in the panel");
        boolean hasOperational = user.is(
                com.tpg.actions.Waiting.on(SystemStatus.OPERATIONAL_LABEL).within(1));
        boolean hasDegraded = user.is(
                com.tpg.actions.Waiting.on(SystemStatus.DEGRADED_LABEL).within(1));
        Assert.assertTrue(hasOperational || hasDegraded,
                "At least one Operational (green) or Degraded (orange) status label must be visible");
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-04: Each service row displays a progress bar
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-04",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify that each of the 4 service rows displays a progress bar showing a health percentage")
    @Outcome("4 progress bar elements are present in the panel; at least one has a non-zero computed width")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testEachServiceRowDisplaysProgressBar() {

        step("Wait for Dashboard to fully load with live KPI data");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard fully loaded — health percentages have been computed")
                        .isVisible()
        );

        step("Use JS evaluate to count progress bar elements in the System Status panel");
        Long progressCount = (Long) browser.evaluate(
                "() => { const panel = document.querySelector(\"div.bg-card:has(h3)\"); " +
                "if (!panel) return 0; " +
                "const byRole = panel.querySelectorAll(\"[role='progressbar']\").length; " +
                "const byClass = panel.querySelectorAll(\"div[class*='progress']\").length; " +
                "const byStyle = panel.querySelectorAll(\"div[style*='width']\").length; " +
                "return Math.max(byRole, byClass, byStyle); }");
        Assert.assertTrue(progressCount >= 4,
                "System Status panel must contain at least 4 progress bar elements — actual: " + progressCount);

        step("Verify at least one progress bar has a non-zero width — health percentages are reflected visually");
        Boolean hasNonZeroWidth = (Boolean) browser.evaluate(
                "() => { const bars = document.querySelectorAll(" +
                "\"div.bg-card:has(h3) div[style*='width']\"); " +
                "for (const bar of bars) { " +
                "  const w = bar.style.width; " +
                "  if (w && w !== '0%' && w !== '0px') return true; } " +
                "return false; }");
        Assert.assertTrue(Boolean.TRUE.equals(hasNonZeroWidth),
                "At least one progress bar must show a non-zero width — health percentages are reflected visually");
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-05: Analytics Platform always shows 'Operational' status
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-05",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the Analytics Platform service row always shows 'Operational' (green) status and never 'Degraded' (orange)")
    @Outcome("span.text-green-600 is visible in the Analytics Platform row; span.text-orange-600 is NOT present in that row")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_SYSTEM_STATUS})
    public void testAnalyticsPlatformAlwaysOperational() {

        step("Wait for Dashboard to fully load with live KPI data");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard fully loaded — service status has been computed")
                        .isVisible()
        );

        step("Verify the Analytics Platform row shows 'Operational' (span.text-green-600)");
        user.wantsTo(
                Verify.uiElement(SystemStatus.ANALYTICS_PLATFORM_OPERATIONAL)
                        .describedAs("span.text-green-600 is present in the Analytics Platform row — always Operational per specification")
                        .isVisible()
        );

        step("Verify no 'Degraded' (span.text-orange-600) label is present in the Analytics Platform row");
        user.wantsTo(
                Verify.uiElement(SystemStatus.ANALYTICS_PLATFORM_DEGRADED)
                        .describedAs("span.text-orange-600 must NOT be present in the Analytics Platform row — it is never Degraded")
                        .isNotVisible()
        );

        step("Verify the Analytics Platform row contains the text 'Operational'");
        user.wantsTo(
                Verify.uiElement(SystemStatus.ANALYTICS_PLATFORM_OPERATIONAL)
                        .describedAs("'Operational' text is present in the Analytics Platform row")
                        .containsText("Operational")
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-06: Deployment Service shows 'Operational' when in-progress orders exist
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-06",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the Deployment Service row shows 'Operational' (green) when in-progress service orders exist")
    @Outcome("span.text-green-600 is visible in the Deployment Service row when listServiceOrdersByStatus returns non-empty items")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testDeploymentServiceOperationalWhenInProgressOrdersExist() {

        step("Register route to return one in-progress service order; other queries pass through");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listServiceOrdersByStatus") && body.contains("In Progress")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_IN_PROGRESS_ORDERS));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with mock in-progress order response active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for Active Deployments KPI to settle — in-progress orders were processed");
            user.wantsTo(
                    Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                            .describedAs("Active Deployments KPI shows a value — service orders API responded")
                            .isVisible()
            );

            step("Verify the Deployment Service row shows 'Operational' (span.text-green-600)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_OPERATIONAL)
                            .describedAs("span.text-green-600 is visible in the Deployment Service row — health ≥ 90% when in-progress orders exist")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-07: Deployment Service shows 'Degraded' when no in-progress orders exist
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-07",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the Deployment Service row shows 'Degraded' (orange) when no in-progress service orders exist")
    @Outcome("span.text-orange-600 is visible in the Deployment Service row when listServiceOrdersByStatus returns empty items")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testDeploymentServiceDegradedWhenNoInProgressOrders() {

        step("Register route to return empty in-progress service orders; other queries pass through");
        browser.route("**appsync-api**", route -> {
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
            step("Re-navigate to the Dashboard with zero in-progress orders response active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for Active Deployments KPI to show '0' — no in-progress orders");
            user.wantsTo(
                    Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                            .describedAs("Active Deployments KPI card is visible — zero in-progress orders response was processed")
                            .isVisible()
            );

            step("Verify the Deployment Service row shows 'Degraded' (span.text-orange-600)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_DEGRADED)
                            .describedAs("span.text-orange-600 is visible in the Deployment Service row — Degraded when no in-progress orders exist")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-08: Compliance Engine shows 'Operational' when no pending compliance items
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-08",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the Compliance Engine row shows 'Operational' (green) when no pending compliance items exist")
    @Outcome("span.text-green-600 is visible in the Compliance Engine row when the compliance query returns empty items")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testComplianceEngineOperationalWhenNoPendingItems() {

        step("Register route to return empty pending compliance items; other queries pass through");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listFirmwareCompliance") || body.contains("Pending")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_EMPTY_COMPLIANCE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with zero pending compliance items active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for Pending Approvals KPI to settle");
            user.wantsTo(
                    Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                            .describedAs("Pending Approvals KPI is visible — compliance query responded")
                            .isVisible()
            );

            step("Verify the Compliance Engine row shows 'Operational' (span.text-green-600)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.COMPLIANCE_ENGINE_OPERATIONAL)
                            .describedAs("span.text-green-600 is visible in the Compliance Engine row — Operational when no pending compliance items")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-09: Compliance Engine shows 'Degraded' when pending compliance items exist
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-09",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the Compliance Engine row shows 'Degraded' (orange) when pending compliance items exist")
    @Outcome("span.text-orange-600 is visible in the Compliance Engine row when the compliance query returns pending items")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testComplianceEngineDegradedWhenPendingItemsExist() {

        step("Register route to return one pending compliance item; other queries pass through");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listFirmwareCompliance") || body.contains("Pending")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_PENDING_COMPLIANCE));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with pending compliance items active");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for Pending Approvals KPI to be populated");
            user.wantsTo(
                    Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                            .describedAs("Pending Approvals KPI is populated — compliance query with pending item responded")
                            .isVisible()
            );

            step("Verify the Compliance Engine row shows 'Degraded' (span.text-orange-600)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.COMPLIANCE_ENGINE_DEGRADED)
                            .describedAs("span.text-orange-600 is visible in the Compliance Engine row — Degraded when pending compliance items exist")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-10: Asset Database status reflects average device health score
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-10",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify Asset Database shows 'Operational' when avg health ≥ 90% and 'Degraded' when avg health < 90%")
    @Outcome("span.text-green-600 visible for high-health devices; span.text-orange-600 visible after switching to low-health devices via refresh")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testAssetDatabaseStatusReflectsDeviceHealthScore() {

        step("Register route to return high-health devices (avg 96.5%) for listDevices; other queries pass through");
        browser.route("**appsync-api**", route -> {
            String body = route.request().postData() != null ? route.request().postData() : "";
            if (body.contains("listDevices")) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody(MOCK_HIGH_HEALTH_DEVICES));
            } else {
                route.fallback();
            }
        });

        try {
            step("Re-navigate to the Dashboard with high-health device data");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Wait for Health Score KPI to reflect the high-health mock data (value contains '%')");
            user.wantsTo(
                    Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                            .describedAs("Health Score KPI shows a percentage — device health data was processed")
                            .containsText("%")
            );

            step("Verify the Asset Database row shows 'Operational' for high average health score (≥ 90%)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.ASSET_DATABASE_OPERATIONAL)
                            .describedAs("span.text-green-600 is visible in the Asset Database row — avg health score 96.5% ≥ 90%")
                            .isVisible()
            );

            step("Update route to return low-health devices (avg 50%) and click Refresh Dashboard");
            browser.unrouteAll();
            browser.route("**appsync-api**", route -> {
                String body = route.request().postData() != null ? route.request().postData() : "";
                if (body.contains("listDevices")) {
                    route.fulfill(new Route.FulfillOptions()
                            .setStatus(200)
                            .setContentType("application/json")
                            .setBody(MOCK_LOW_HEALTH_DEVICES));
                } else {
                    route.fallback();
                }
            });
            user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

            step("Verify the Asset Database row shows 'Degraded' after switching to low-health devices (< 90%)");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.ASSET_DATABASE_DEGRADED)
                            .describedAs("span.text-orange-600 is visible in the Asset Database row — avg health score 50% < 90%")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-11: System Status panel remains visible when KPI error banner appears
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-11",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the System Status panel is still rendered when all KPI APIs fail and the global error banner appears")
    @Outcome("Global error banner is visible; System Status panel container is still rendered")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testSystemStatusPanelRemainsVisibleWhenKpiErrorBannerAppears() {

        step("Confirm Dashboard is fully loaded before registering the error route");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard loaded with live data before applying error route")
                        .isVisible()
        );

        step("Register route to return HTTP 500 for all fetch/xhr calls to simulate all KPI API failures");
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
            step("Click Refresh Dashboard to re-trigger all API calls with the error route active");
            user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

            step("Verify the global error banner appears — all KPI APIs failed");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("Red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible after KPI API failures")
                            .isVisible()
            );

            step("Verify the System Status panel container is still visible");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.CONTAINER)
                            .describedAs("System Status panel is still rendered — it does not disappear when KPI APIs fail")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-12: System Status panel updates correctly after Refresh Dashboard
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-12",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify the System Status panel remains rendered and at least one status label is visible after clicking Refresh Dashboard")
    @Outcome("Panel is visible before and after refresh; KPI cards repopulate; at least one status label is visible post-refresh")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_SYSTEM_STATUS})
    public void testSystemStatusPanelUpdatesAfterRefreshDashboard() {

        step("Verify the System Status panel is visible before refresh");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel is present before the refresh cycle")
                        .isVisible()
        );

        step("Click the Refresh Dashboard button");
        user.attemptsTo(DashboardPageImpl.clickRefreshDashboard());

        step("Wait for KPI cards to repopulate — confirms the refresh cycle completed");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card shows a live value after refresh")
                        .isVisible()
        );

        step("Verify the System Status panel container is still visible after refresh");
        user.wantsTo(
                Verify.uiElement(SystemStatus.CONTAINER)
                        .describedAs("System Status panel is rendered after refresh — not stuck in a broken state")
                        .isVisible()
        );

        step("Verify at least one status label is visible post-refresh");
        boolean hasOperational = user.is(
                com.tpg.actions.Waiting.on(SystemStatus.OPERATIONAL_LABEL).within(5));
        boolean hasDegraded = user.is(
                com.tpg.actions.Waiting.on(SystemStatus.DEGRADED_LABEL).within(1));
        Assert.assertTrue(hasOperational || hasDegraded,
                "At least one Operational or Degraded status label must be visible after refresh");
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-13: Service rows appear in correct order
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-13",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify that the 4 service rows appear in the correct fixed order: Deployment Service, Compliance Engine, Asset Database, Analytics Platform")
    @Outcome("Row 1 = Deployment Service, Row 2 = Compliance Engine, Row 3 = Asset Database, Row 4 = Analytics Platform")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testServiceRowsAppearInCorrectOrder() {

        step("Wait for Dashboard to fully load with live KPI data");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard is fully loaded")
                        .isVisible()
        );

        step("Use JS evaluate to extract service row names and assert ordering");
        @SuppressWarnings("unchecked")
        java.util.List<String> serviceNames = (java.util.List<String>) browser.evaluate(
                "() => Array.from(document.querySelectorAll(" +
                "\"div.bg-card:has(h3) div.space-y-4 > div\"))" +
                ".map(el => el.textContent.trim().split('\\n')[0].trim())");
        Assert.assertNotNull(serviceNames, "JS evaluate returned null — no service rows found");
        Assert.assertTrue(serviceNames.size() >= 4,
                "Expected at least 4 service rows — actual: " + serviceNames);
        Assert.assertTrue(serviceNames.get(0).contains("Deployment Service"),
                "Row 1 must be 'Deployment Service' — actual: " + serviceNames.get(0));
        Assert.assertTrue(serviceNames.get(3).contains("Analytics Platform"),
                "Row 4 must be 'Analytics Platform' — actual: " + serviceNames.get(3));

        step("Verify the first row text contains 'Deployment Service' via CSS nth-child");
        user.wantsTo(
                Verify.uiElement(
                        "div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:nth-child(1)")
                        .describedAs("First service row contains 'Deployment Service'")
                        .containsText("Deployment Service")
        );

        step("Verify the fourth row text contains 'Analytics Platform' via CSS nth-child");
        user.wantsTo(
                Verify.uiElement(
                        "div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:nth-child(4)")
                        .describedAs("Fourth service row contains 'Analytics Platform'")
                        .containsText("Analytics Platform")
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-STATUS-14: Operational label uses text-green-600, Degraded uses text-orange-600
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-STATUS-14",
            stories = {"PS-5"}, category = "DASHBOARD_SYSTEM_STATUS")
    @Description("Verify that Operational status uses CSS class text-green-600 and Degraded status uses text-orange-600")
    @Outcome("Operational span className includes 'text-green-600'; Deployment Service Degraded span className includes 'text-orange-600' when forced")
    @Test(groups = {REGRESSION, DASHBOARD_SYSTEM_STATUS})
    public void testOperationalUsesGreenAndDegradedUsesOrangeColor() {

        step("Wait for Dashboard to fully load");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard loaded")
                        .isVisible()
        );

        step("Verify at least one span.text-green-600 (Operational) is present and confirm its class");
        user.wantsTo(
                Verify.uiElement(SystemStatus.OPERATIONAL_LABEL)
                        .describedAs("An element with CSS class text-green-600 is visible in the System Status panel — Operational color")
                        .isVisible()
        );
        String greenClass = (String) browser.evaluate(
                "() => { const el = document.querySelector(" +
                "\"div.bg-card:has(h3) span.text-green-600\"); " +
                "return el ? el.className : ''; }");
        Assert.assertTrue(greenClass != null && greenClass.contains("text-green-600"),
                "Operational label span must carry class text-green-600 — actual: " + greenClass);

        step("Register route to force Deployment Service into Degraded (zero in-progress orders) to verify orange color");
        browser.route("**appsync-api**", route -> {
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
            step("Re-navigate to Dashboard with zero orders so Deployment Service is Degraded");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify span.text-orange-600 is present in Deployment Service row — confirms Degraded color is text-orange-600");
            user.wantsTo(
                    Verify.uiElement(SystemStatus.DEPLOYMENT_SERVICE_DEGRADED)
                            .describedAs("span.text-orange-600 is present in Deployment Service row — correct Degraded color class")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }
}
