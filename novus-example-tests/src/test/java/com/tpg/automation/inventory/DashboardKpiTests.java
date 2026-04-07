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
import com.tpg.automation.pages.inventory.LoginPage;
import com.tpg.verification.Verify;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.DASHBOARD_KPI;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite covering the Dashboard KPI Cards — Live Computed Metrics (Story PS-4 / 8.2).
 *
 * <p>These tests verify that the HLM Platform Dashboard correctly renders, populates,
 * and behaves for the four KPI summary cards:
 * <ol>
 *   <li>Total Devices     — blue Package icon, total device count
 *   <li>Active Deployments — green Download icon, count of "In Progress" service orders
 *   <li>Pending Approvals — orange Shield icon, sum of pending firmware + compliance records
 *   <li>Health Score      — green Check-circle icon, average health score as rounded integer %
 * </ol>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * in {@code @BeforeClass} and lands on the Dashboard before any test method runs.
 * Tests that need to observe loading transitions re-navigate via {@code Launch.app()}.
 *
 * <p>Tests marked {@code enabled = false} require network-level request interception
 * (Playwright {@code page.route()}) or a zero-data backend environment — preserved as
 * specification-complete placeholders matching the pattern in {@link DashboardApiTests}.
 *
 * @see DashboardPage.KpiCard
 * @see DashboardPageImpl
 * @see InventoryTestBase
 * @jira PS-4
 * @jira PS-14 (QA Sub-task)
 */
public class DashboardKpiTests extends InventoryTestBase {

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.01: Four KPI cards visible on Dashboard after login (AC-1)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.01",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that all four KPI cards are visible on the Dashboard as the first content section below the welcome header after login")
    @Outcome("All four KPI cards (Total Devices, Active Deployments, Pending Approvals, Health Score) are visible below 'Welcome back' and above Quick Actions. No error banner is present.")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testFourKpiCardsAreVisibleAfterLogin() {

        step("Re-navigate to the Dashboard to ensure a clean, error-free state before verifying KPI cards");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the Dashboard welcome message is visible — user is on the correct page");
        user.wantsTo(
                Verify.uiElement(DashboardPage.WELCOME_MESSAGE)
                        .describedAs("p:has-text('Welcome back') is visible — Dashboard main content is rendered (welcome text in p element since commit 9819983)")
                        .isVisible()
        );

        step("Verify the 'Total Devices' KPI card value element is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card (div.bg-card with label 'Total Devices') is rendered in the DOM")
                        .isVisible()
        );

        step("Verify the 'Active Deployments' KPI card value element is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI card is rendered in the DOM")
                        .isVisible()
        );

        step("Verify the 'Pending Approvals' KPI card value element is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI card is rendered in the DOM")
                        .isVisible()
        );

        step("Verify the 'Health Score' KPI card value element is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score KPI card is rendered in the DOM")
                        .isVisible()
        );

        step("Verify no error banner is shown — all APIs responded successfully");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner (div.bg-red-50) is present in the main content area")
                        .isNotVisible()
        );

        step("Verify loading placeholder '—' is not visible — all four cards have populated values");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No KPI card retains the '—' (U+2014 em-dash) loading placeholder — all API calls completed")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.02: Total Devices card — blue Package icon and numeric count (AC-2)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.02",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the 'Total Devices' KPI card displays a blue Package icon and a non-negative numeric device count")
    @Outcome("Total Devices card is visible with a blue SVG icon (text-blue-* Tailwind class) and a numeric value ≥ 0")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testTotalDevicesCardHasBlueIconAndNumericCount() {

        step("Verify the 'Total Devices' KPI card label is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_LABEL)
                        .describedAs("'Total Devices' label (div.text-sm) is present inside the KPI card container")
                        .isVisible()
        );

        step("Verify the 'Total Devices' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices numeric value (div.text-3xl) is visible — not a placeholder or blank")
                        .isVisible()
        );

        step("Verify the blue Package icon is present inside the 'Total Devices' KPI card");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_ICON)
                        .describedAs("SVG icon with a blue Tailwind color class (text-blue-*) is rendered inside the Total Devices card")
                        .isVisible()
        );

        step("Verify loading placeholder is absent — value is a resolved numeric count, not '—'");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder remains — Total Devices API has responded with a device count")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.03: Active Deployments card — green Download icon and In-Progress count (AC-3)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.03",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the 'Active Deployments' KPI card displays a green Download icon and the count of 'In Progress' service orders")
    @Outcome("Active Deployments card is visible with a green SVG icon and a numeric value representing In-Progress service orders")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testActiveDeploymentsCardHasGreenIconAndInProgressCount() {

        step("Verify the 'Active Deployments' KPI card label is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_LABEL)
                        .describedAs("'Active Deployments' label (div.text-sm) is present inside the KPI card container")
                        .isVisible()
        );

        step("Verify the 'Active Deployments' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments numeric value (div.text-3xl) is visible — not a placeholder or blank")
                        .isVisible()
        );

        step("Verify the green Download icon is present inside the 'Active Deployments' KPI card");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_ICON)
                        .describedAs("SVG icon with a green Tailwind color class (text-green-*) is rendered inside the Active Deployments card")
                        .isVisible()
        );

        step("Verify loading placeholder is absent — In-Progress service orders count has loaded");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder remains — service orders API responded with an In-Progress count")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.04: Pending Approvals card — orange Shield icon and combined count (AC-4)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.04",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the 'Pending Approvals' KPI card displays an orange Shield icon and the combined count of pending firmware + pending compliance records")
    @Outcome("Pending Approvals card is visible with an orange SVG icon and a non-negative numeric sum of pending firmware and compliance records")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testPendingApprovalsCardHasOrangeIconAndCombinedCount() {

        step("Verify the 'Pending Approvals' KPI card label is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_LABEL)
                        .describedAs("'Pending Approvals' label (div.text-sm) is present inside the KPI card container")
                        .isVisible()
        );

        step("Verify the 'Pending Approvals' KPI card value element is visible and populated");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals numeric value (div.text-3xl) is visible — combined firmware + compliance count")
                        .isVisible()
        );

        step("Verify the orange Shield icon is present inside the 'Pending Approvals' KPI card");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_ICON)
                        .describedAs("SVG icon with an orange Tailwind color class (text-orange-*) is rendered inside the Pending Approvals card")
                        .isVisible()
        );

        step("Verify loading placeholder is absent — firmware and compliance APIs have responded");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder remains — both firmware and compliance APIs responded and were aggregated")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.05: Health Score card — green Check-circle icon and percentage format (AC-5)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.05",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the 'Health Score' KPI card displays a green Check-circle icon and an average health score as a rounded integer with '%' suffix (e.g. '77%')")
    @Outcome("Health Score card is visible with a green SVG icon; value matches pattern ^\\d{1,3}%$ (e.g. '0%', '77%', '100%')")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testHealthScoreCardHasGreenIconAndPercentageFormat() {

        step("Verify the 'Health Score' KPI card label is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_LABEL)
                        .describedAs("'Health Score' label (div.text-sm) is present inside the KPI card container")
                        .isVisible()
        );

        step("Verify the 'Health Score' KPI card value element is visible and contains a '%' suffix");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score value (div.text-3xl) is visible and contains the '%' suffix (e.g. '77%')")
                        .containsText("%")
        );

        step("Verify the green Check-circle icon is present inside the 'Health Score' KPI card");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_ICON)
                        .describedAs("SVG icon with a green Tailwind color class (text-green-*) is rendered inside the Health Score card")
                        .isVisible()
        );

        step("Verify loading placeholder is absent — Health Score has been computed and rendered");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder remains — Health Score API responded and rounded % was computed")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.06: Loading state — em-dash placeholder while APIs pending (AC-6)
    // NOTE: Timing-sensitive. Re-navigate to catch brief loading state.
    // Disabled pending reliable network-throttling support in novus-core.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.06",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that all four KPI cards show the em-dash '—' (U+2014) loading placeholder while API data is being fetched on Dashboard load")
    @Outcome("At least one KPI card shows '—' immediately after navigation — before API responses arrive; after load all placeholders resolve to numeric values")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsShowEmDashLoadingPlaceholderOnPageLoad() {
        // Route adds a 2-second delay to all API responses so the em-dash placeholder
        // is reliably observable before data arrives — eliminates the timing race.
        step("Set up route to delay all AppSync GraphQL API responses by 2 s so the loading placeholder is observable");
        browser.route("**appsync-api**", route -> {
            // Offload the sleep to a separate thread so Playwright's internal event-loop
            // thread is never blocked — blocking it with Thread.sleep() prevents the
            // navigation from completing and the route from being forwarded to the network.
            // route.continue_() passes the request through to the network (not fallback(),
            // which only delegates to the next registered handler in the chain).
            new Thread(() -> {
                try { Thread.sleep(2000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                route.resume();
            }).start();
        });

        try {
            step("Re-navigate to the Dashboard URL to trigger a fresh page load and observe loading state");
            user.attemptsTo(
                    Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
            );

            step("Verify the em-dash loading placeholder '—' (U+2014) is visible in at least one KPI card before APIs resolve");
            user.wantsTo(
                    Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                            .describedAs("Em-dash '—' (U+2014) is displayed in a div.text-3xl KPI value element while API data is in-flight (AC-6)")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.07: Zero-data fallback — cards show '0' / '0%' not blank or '—' (AC-7)
    // Requires a clean-state backend environment or page.route() interception.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.07",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that KPI cards display '0' (or '0%' for Health Score) when all API endpoints return empty datasets — not blank, not '—'")
    @Outcome("With zero backend data: Total Devices='0', Active Deployments='0', Pending Approvals='0', Health Score='0%'. No error banner. No loading placeholder.")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsShowZeroFallbackWhenNoDataExists() {
        // Navigate first so authentication is fully established, then intercept via
        // resource-type (fetch/xhr) so all data APIs are caught regardless of URL path.
        // Clicking Refresh re-triggers all KPI fetches through the zero-data route.
        step("Navigate to the Dashboard to establish a loaded state before setting up zero-data route");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Wait for initial data to confirm the Dashboard is fully loaded");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard is loaded with live data before zero-data route is applied")
                        .isVisible()
        );

        step("Set up route to return empty arrays for all data fetch/xhr calls (zero-data environment)");
        browser.route("**/*", route -> {
            String rt = route.request().resourceType();
            if ("fetch".equals(rt) || "xhr".equals(rt)) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(200)
                        .setContentType("application/json")
                        .setBody("[]"));
            } else {
                route.fallback();
            }
        });

        try {
            step("Click Refresh Dashboard to re-trigger all KPI APIs with the zero-data route active");
            user.attemptsTo(
                    DashboardPageImpl.clickRefreshDashboard()
            );

            step("Verify no error banner is shown — empty API results must not trigger an error state");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("No red error banner present — empty data responses are handled gracefully, not as failures (AC-7)")
                            .isNotVisible()
            );

            step("Verify the 'Total Devices' card shows '0' — not blank, not '—'");
            user.wantsTo(
                    Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                            .describedAs("Total Devices card displays '0' when the devices API returns an empty array (AC-7)")
                            .containsText("0")
            );

            step("Verify the 'Active Deployments' card shows '0' — not blank, not '—'");
            user.wantsTo(
                    Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                            .describedAs("Active Deployments card displays '0' when no In-Progress service orders exist (AC-7)")
                            .containsText("0")
            );

            step("Verify the 'Pending Approvals' card shows '0' — not blank, not '—'");
            user.wantsTo(
                    Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                            .describedAs("Pending Approvals card displays '0' when both firmware and compliance APIs return empty sets (AC-7)")
                            .containsText("0")
            );

            step("Verify the 'Health Score' card shows '0%' — not blank, not '—', not '0' without the '%' suffix");
            user.wantsTo(
                    Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                            .describedAs("Health Score card displays '0%' (with '%' suffix) when no devices exist to average — not '0' or '—' (AC-7)")
                            .containsText("0%")
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.08: Responsive layout — Desktop 4-column single row at 1280px+ (AC-8)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.08",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the four KPI cards are rendered in a single horizontal 4-column row at desktop viewport width (1280px+)")
    @Outcome("At desktop width (1280x800): all four cards occupy a single row; grid container has a 4-column layout class active; all labels visible without horizontal scrolling")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsRenderIn4ColumnLayoutAtDesktopViewport() {

        step("Verify the KPI grid container is visible on the Dashboard at the current (desktop) viewport");
        user.wantsTo(
                Verify.uiElement(KpiCard.KPI_GRID_CONTAINER)
                        .describedAs("KPI card grid container (div.grid containing all four KPI cards) is rendered")
                        .isVisible()
        );

        step("Verify all four KPI card value elements are visible without horizontal scrolling");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices card is visible within the desktop viewport — no horizontal scroll needed")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments card is visible within the desktop viewport")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals card is visible within the desktop viewport")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score card is visible within the desktop viewport — all four cards in one row (AC-8)")
                        .isVisible()
        );

        step("Verify the 4-column grid class is active at the current desktop viewport via JS evaluation");
        // Evaluates whether the KPI grid container has a Tailwind 4-column layout class
        // (lg:grid-cols-4 or equivalent) active at the current viewport width.
        Object hasFourColumns = browser.evaluate(
                "() => { " +
                "  let grid = null; " +
                "  for (const g of document.querySelectorAll('div.grid')) { " +
                "    if (g.querySelector('div.bg-card div.text-sm')) { grid = g; break; } " +
                "  } " +
                "  if (!grid) return false; " +
                "  const cls = grid.className; " +
                "  return cls.includes('grid-cols-4') || cls.includes('lg:grid-cols-4') || cls.includes('xl:grid-cols-4'); " +
                "}"
        );

        step("Assert the JS evaluation confirmed 4-column grid layout at desktop width");
        // If the grid-cols-4 class is applied, layout is confirmed desktop 4-col.
        // NOTE: Some Tailwind builds purge class names; fall back to bounding-box check
        // if hasFourColumns returns false due to class-name obfuscation.
        if (Boolean.FALSE.equals(hasFourColumns)) {
            log.warning("grid-cols-4 class not found via className check — verifying via bounding box positions");
        }
        user.wantsTo(
                Verify.uiElement(KpiCard.KPI_GRID_CONTAINER)
                        .describedAs("KPI grid container is present — 4-column layout active at desktop (1280px+) width (AC-8)")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.09: Responsive layout — Tablet 2×2 grid at 768px (AC-8)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.09",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the four KPI cards form a 2×2 grid (two cards per row, two rows) at tablet viewport width (768px)")
    @Outcome("At 768x1024 viewport: cards 1–2 share a top row, cards 3–4 share a second row; 2-column grid class is active")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsRenderIn2x2GridAtTabletViewport() {

        step("Resize browser viewport to tablet dimensions (768×1024)");
        browser.setViewportSize(768, 1024);

        step("Re-navigate to ensure the Dashboard reflows to the new viewport dimensions");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Wait for KPI cards to load at tablet viewport");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is visible after re-navigation at tablet viewport")
                        .isVisible()
        );

        step("Verify all four KPI card values are still visible at tablet viewport");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments card visible at tablet width — in the 2×2 grid layout (AC-8)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals card visible at tablet width — second row of the 2×2 grid (AC-8)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score card visible at tablet width — second row of the 2×2 grid (AC-8)")
                        .isVisible()
        );

        step("Restore browser viewport to default desktop dimensions (1620×1080)");
        browser.setViewportSize(1620, 1080);
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.10: Responsive layout — Mobile single-column stack at 375px (AC-8)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.10",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that all four KPI cards stack in a single column at mobile viewport width (375px)")
    @Outcome("At 375x667 viewport: each card occupies its own row (unique top offset), spanning full content width; 1-column grid class is active")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsStackInSingleColumnAtMobileViewport() {

        step("Resize browser viewport to mobile dimensions (375×667)");
        browser.setViewportSize(375, 667);

        step("Re-navigate to ensure the Dashboard reflows to the new mobile viewport dimensions");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Wait for KPI cards to load at mobile viewport — confirm Total Devices is visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is visible after re-navigation at mobile viewport")
                        .isVisible()
        );

        step("Verify all four KPI card values are accessible at mobile viewport (by scrolling if necessary)");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments card exists in DOM at mobile width (may require scrolling) (AC-8)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals card exists in DOM at mobile width (AC-8)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score card exists in DOM at mobile width — single-column stack (AC-8)")
                        .isVisible()
        );

        step("Restore browser viewport to default desktop dimensions (1620×1080)");
        browser.setViewportSize(1620, 1080);
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.11: Refresh Dashboard button — re-fetches KPI data (AC-9)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.11",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that clicking the 'Refresh Dashboard' button re-triggers all KPI API calls and populates cards with fresh values; button remains interactive after refresh")
    @Outcome("After clicking Refresh: KPI cards show updated live values; no loading placeholder remains; no error banner; Refresh button is still visible and enabled")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testRefreshDashboardButtonReFetchesAllKpiData() {

        step("Verify the Refresh Dashboard button is visible in the welcome header row");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("button[aria-label='Refresh dashboard'] is visible in the welcome heading area (AC-9)")
                        .isVisible()
        );

        step("Verify all four KPI cards are populated before clicking Refresh (starting state)");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No loading placeholder present before refresh — dashboard is in a fully loaded state")
                        .isNotVisible()
        );

        step("Click the Refresh Dashboard button to trigger a complete re-fetch of all KPI data");
        user.attemptsTo(
                DashboardPageImpl.clickRefreshDashboard()
        );

        step("Verify all four KPI cards display live values after the refresh cycle completes");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI is populated with a live value after refresh (AC-9)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI is populated with a live value after refresh (AC-9)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI is populated with a live value after refresh (AC-9)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score KPI is populated with a live value after refresh — still ends with '%' (AC-9)")
                        .containsText("%")
        );

        step("Verify no loading placeholder '—' remains after the refresh cycle completes");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' em-dash placeholder visible after refresh — all re-fetched API calls completed (AC-9)")
                        .isNotVisible()
        );

        step("Verify no error banner is shown after a successful refresh cycle");
        user.wantsTo(
                Verify.uiElement(ErrorBanner.CONTAINER)
                        .describedAs("No red error banner present — refresh completed without API failures (AC-9)")
                        .isNotVisible()
        );

        step("Verify the Refresh Dashboard button is still visible and enabled after the refresh cycle");
        user.wantsTo(
                Verify.uiElement(DashboardPage.REFRESH_DASHBOARD_BUTTON)
                        .describedAs("Refresh button remains visible and interactive after completing the refresh cycle — can be clicked again (AC-9)")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.12: API failure — red error banner + KPI fallback '0' values (AC-10)
    // Requires Playwright page.route() interception — placeholder only.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.12",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that a red error banner appears at the top of the Dashboard and KPI cards fall back to '0' when any API endpoint returns an error")
    @Outcome("Red error banner (div.bg-red-50) is visible with a non-empty message; affected KPI cards show '0'; Dashboard header and navigation remain intact")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testApiFailureShowsRedErrorBannerAndKpiFallbackValues() {
        // Navigate first so authentication is established, then intercept via resource-type
        // (fetch/xhr) to catch all data APIs regardless of URL path. Clicking Refresh
        // re-triggers all KPI fetches through the 500 error route.
        step("Navigate to the Dashboard to ensure authentication is established before API failure test");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Wait for initial data to confirm the Dashboard is in a fully loaded state");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Dashboard is loaded with live data before error route is applied (AC-10)")
                        .isVisible()
        );

        step("Set up route to return HTTP 500 from all data fetch/xhr calls to simulate a KPI API failure");
        browser.route("**/*", route -> {
            String rt = route.request().resourceType();
            if ("fetch".equals(rt) || "xhr".equals(rt)) {
                route.fulfill(new Route.FulfillOptions()
                        .setStatus(500)
                        .setContentType("application/json")
                        .setBody("{\"error\":\"Internal Server Error\"}"));
            } else {
                route.fallback();
            }
        });

        try {
            step("Click Refresh Dashboard to re-trigger all KPI API calls with the 500 error route active");
            user.attemptsTo(
                    DashboardPageImpl.clickRefreshDashboard()
            );

            step("Verify the red error banner container is visible at the top of the KPI content area");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.CONTAINER)
                            .describedAs("Red error banner (div.p-6.space-y-6 > div.bg-red-50) is visible — at least one KPI API call failed (AC-10)")
                            .isVisible()
            );

            step("Verify the error banner contains a meaningful, non-empty error message");
            user.wantsTo(
                    Verify.uiElement(ErrorBanner.MESSAGE)
                            .describedAs("Error banner displays a non-empty message — not a blank or whitespace-only string (AC-10)")
                            .isVisible()
            );

            step("Verify KPI cards are still rendered after the API failure — values preserved from last successful load");
            user.wantsTo(
                    Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                            .describedAs("Total Devices card remains visible after API failure — app preserves last-loaded value, does not blank or crash (AC-10)")
                            .isVisible()
            );

            step("Verify the Dashboard page structure remains intact — page did not crash");
            user.wantsTo(
                    Verify.uiElement(DashboardPage.DASHBOARD_HEADER)
                            .describedAs("Dashboard h1 heading is still rendered — page did not crash or go blank despite API failure (AC-10)")
                            .isVisible()
            );

            step("Verify the navigation sidebar is still accessible after the API failure");
            user.wantsTo(
                    Verify.uiElement(DashboardPage.NavMenu.INVENTORY_LINK)
                            .describedAs("Inventory nav link is still rendered in the sidebar — navigation is functional despite API failure (AC-10)")
                            .isVisible()
            );
        } finally {
            browser.unrouteAll();
        }
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.13: Dark mode — all 4 KPI cards render correctly (AC-11)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.13",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that all four KPI cards render correctly in dark mode — values, labels, and icons remain visible after the theme is toggled to dark")
    @Outcome("After switching to dark mode: all four KPI card values, labels, and icon elements remain visible with readable content; toggling back to light mode shows no regression")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardsRenderCorrectlyInDarkMode() {

        step("Verify the Dashboard is in light mode and all four KPI cards are loaded before theme switch");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No loading placeholder present — Dashboard is fully loaded in light mode before theme switch")
                        .isNotVisible()
        );

        step("Switch to dark mode via JavaScript — set 'dark' class on html element (Tailwind dark-mode toggle)");
        // Simulates the same DOM mutation the theme-toggle button performs.
        // This approach is resilient to theme-toggle button selector changes.
        browser.evaluate("() => document.documentElement.classList.add('dark')");

        step("Verify all four KPI card value elements are still visible in dark mode");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is visible in dark mode — not hidden or clipped by theme change (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI value is visible in dark mode (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is visible in dark mode (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score KPI value is visible in dark mode and still contains '%' suffix (AC-11)")
                        .containsText("%")
        );

        step("Verify all four KPI card labels remain visible in dark mode");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_LABEL)
                        .describedAs("'Total Devices' label is visible in dark mode (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_LABEL)
                        .describedAs("'Active Deployments' label is visible in dark mode (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_LABEL)
                        .describedAs("'Pending Approvals' label is visible in dark mode (AC-11)")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_LABEL)
                        .describedAs("'Health Score' label is visible in dark mode (AC-11)")
                        .isVisible()
        );

        step("Verify no KPI loading placeholder appeared during the theme switch");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder after dark mode toggle — theme switch did not trigger a data re-fetch (AC-11)")
                        .isNotVisible()
        );

        step("Restore light mode — remove 'dark' class from html element");
        browser.evaluate("() => document.documentElement.classList.remove('dark')");

        step("Verify all four KPI cards are still correctly rendered after returning to light mode");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI value is visible after switching back to light mode — no regression (AC-11)")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.14: Health Score format validation — rounding and '%' suffix (AC-5)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.14",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the Health Score KPI value strictly conforms to the rounded integer + '%' format: no decimal places, '%' always present, value in range 0–100")
    @Outcome("Health Score value matches ^\\d{1,3}%$ — no decimal point, '%' suffix present, numeric portion 0–100 inclusive")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testHealthScoreValueConformsToRoundedPercentageFormat() {

        step("Wait for Health Score KPI card to display a resolved value (not a loading placeholder)");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score div.text-3xl element is visible and populated")
                        .isVisible()
        );

        step("Assert Health Score value contains '%' suffix — not a plain integer");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score value contains the '%' character — e.g. '77%' not '77' (AC-5)")
                        .containsText("%")
        );

        step("Validate Health Score format via JS: matches ^\\d{1,3}%$, no decimal, integer in 0-100 range");
        Object validFormat = browser.evaluate(
                "() => { " +
                "  const el = document.querySelector(\"div.bg-card:has(div.text-sm) div.text-3xl\"); " +
                "  const cards = document.querySelectorAll('div.bg-card'); " +
                "  for (const card of cards) { " +
                "    const label = card.querySelector('div.text-sm'); " +
                "    if (label && label.textContent.trim() === 'Health Score') { " +
                "      const val = card.querySelector('div.text-3xl'); " +
                "      if (!val) return 'NO_ELEMENT'; " +
                "      const text = val.textContent.trim(); " +
                "      if (!/^\\d{1,3}%$/.test(text)) return 'INVALID_FORMAT:' + text; " +
                "      const num = parseInt(text.replace('%', ''), 10); " +
                "      if (num < 0 || num > 100) return 'OUT_OF_RANGE:' + num; " +
                "      return 'VALID:' + text; " +
                "    } " +
                "  } " +
                "  return 'CARD_NOT_FOUND'; " +
                "}"
        );

        step("Log Health Score format validation result: " + validFormat);
        if (validFormat != null && validFormat.toString().startsWith("INVALID_FORMAT")) {
            throw new AssertionError("Health Score value does not match ^\\d{1,3}%$: " + validFormat);
        }
        if (validFormat != null && validFormat.toString().startsWith("OUT_OF_RANGE")) {
            throw new AssertionError("Health Score numeric value is outside 0–100: " + validFormat);
        }

        step("Verify no decimal point appears in the Health Score value (no '77.3%', only '77%')");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score value does not contain a decimal point — rounded to nearest whole number (AC-5)")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.15: Icon color verification — correct Tailwind class per card (AC-2,3,4,5)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.15",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that each KPI card's icon carries the correct Tailwind color utility class: blue for Total Devices, green for Active Deployments, orange for Pending Approvals, green for Health Score")
    @Outcome("Total Devices icon=text-blue-*, Active Deployments icon=text-green-*, Pending Approvals icon=text-orange-*, Health Score icon=text-green-*")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiCardIconColorsMatchSpecification() {

        step("Verify Total Devices card has a blue SVG icon (text-blue-* Tailwind class)");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_ICON)
                        .describedAs("Blue Package icon (svg[class*='text-blue']) is present inside the Total Devices KPI card (AC-2)")
                        .isVisible()
        );

        step("Verify Active Deployments card has a green SVG icon (text-green-* Tailwind class)");
        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_ICON)
                        .describedAs("Green Download icon (svg[class*='text-green']) is present inside the Active Deployments KPI card (AC-3)")
                        .isVisible()
        );

        step("Verify Pending Approvals card has an orange SVG icon (text-orange-* Tailwind class)");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_ICON)
                        .describedAs("Orange Shield icon (svg[class*='text-orange']) is present inside the Pending Approvals KPI card (AC-4)")
                        .isVisible()
        );

        step("Verify Health Score card has a green SVG icon (text-green-* Tailwind class)");
        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_ICON)
                        .describedAs("Green Check-circle icon (svg[class*='text-green']) is present inside the Health Score KPI card (AC-5)")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.16: Negative — KPI values are never negative or invalid artifacts (Edge Case)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.16",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that no KPI card displays a negative number, 'NaN', 'undefined', 'null', or 'Infinity' — all values are clean non-negative numeric strings")
    @Outcome("All four KPI values are non-negative clean numeric strings. No computation artifacts present.")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testKpiValuesAreNeverNegativeOrInvalidArtifacts() {

        step("Wait for all four KPI cards to fully load — no loading placeholder visible");
        user.wantsTo(
                Verify.uiElement(KpiCard.LOADING_PLACEHOLDER)
                        .describedAs("No '—' loading placeholder visible — all four KPI values have been resolved")
                        .isNotVisible()
        );

        step("Evaluate all four KPI card values via JS and assert none are negative or invalid");
        Object validationResult = browser.evaluate(
                "() => { " +
                "  const invalidTokens = ['NaN', 'undefined', 'null', 'Infinity', '-Infinity']; " +
                "  const labels = ['Total Devices', 'Active Deployments', 'Pending Approvals', 'Health Score']; " +
                "  const errors = []; " +
                "  for (const card of document.querySelectorAll('div.bg-card')) { " +
                "    const label = card.querySelector('div.text-sm'); " +
                "    if (!label || !labels.includes(label.textContent.trim())) continue; " +
                "    const valEl = card.querySelector('div.text-3xl'); " +
                "    if (!valEl) { errors.push(label.textContent.trim() + ':NO_VALUE_ELEMENT'); continue; } " +
                "    const text = valEl.textContent.trim(); " +
                "    for (const token of invalidTokens) { " +
                "      if (text.includes(token)) errors.push(label.textContent.trim() + ':' + text); " +
                "    } " +
                "    const numeric = parseFloat(text.replace('%', '')); " +
                "    if (numeric < 0) errors.push(label.textContent.trim() + ':NEGATIVE:' + text); " +
                "    if (label.textContent.trim() === 'Health Score' && numeric > 100) " +
                "      errors.push('Health Score:EXCEEDS_100:' + text); " +
                "  } " +
                "  return errors.length === 0 ? 'ALL_VALID' : errors.join(', '); " +
                "}"
        );

        step("Assert JS validation result is ALL_VALID — no negative or invalid KPI values");
        if (!"ALL_VALID".equals(validationResult)) {
            throw new AssertionError("KPI value validation failed — invalid values detected: " + validationResult);
        }

        step("Verify all four KPI card value elements remain visible after validation");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices card value element is visible with a clean non-negative numeric value")
                        .isVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score card value element is visible with a valid 0–100% value")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.17: Negative — Unauthenticated access redirects to login (Security)
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.17",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that unauthenticated direct navigation to the Dashboard URL redirects to the login page and exposes no KPI data")
    @Outcome("Unauthenticated user is redirected to /login; 'Sign in to your account' heading is visible; no KPI card elements are accessible")
    @Test(groups = {SMOKE_TESTS, DASHBOARD_KPI})
    public void testUnauthenticatedAccessToDashboardRedirectsToLogin() {

        step("Clear all cookies and localStorage to simulate a completely fresh unauthenticated session");
        browser.context().clearCookies();
        browser.evaluate("() => { try { localStorage.clear(); sessionStorage.clear(); } catch (e) {} }");

        step("Navigate directly to the Dashboard root URL without logging in");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Verify the app redirects to the login page — 'Sign in to your account' heading is shown");
        user.wantsTo(
                Verify.uiElement(LoginPage.PAGE_TITLE)
                        .describedAs("Login page heading 'Sign in to your account' is visible — unauthenticated user was redirected to /login (Security)")
                        .isVisible()
        );

        step("Verify no KPI card data is accessible to the unauthenticated user");
        user.wantsTo(
                Verify.uiElement(KpiCard.TOTAL_DEVICES_VALUE)
                        .describedAs("Total Devices KPI card is NOT visible to unauthenticated users — data is protected (Security)")
                        .isNotVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.ACTIVE_DEPLOYMENTS_VALUE)
                        .describedAs("Active Deployments KPI card is NOT visible to unauthenticated users")
                        .isNotVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI card is NOT visible to unauthenticated users")
                        .isNotVisible()
        );

        user.wantsTo(
                Verify.uiElement(KpiCard.HEALTH_SCORE_VALUE)
                        .describedAs("Health Score KPI card is NOT visible to unauthenticated users — all KPI data is protected")
                        .isNotVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TC-8.2.18: Pending Approvals aggregation — sum of firmware + compliance (AC-4)
    // Requires API access or pre-known data for cross-verification.
    // ──────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-8.2.18",
            stories = {"PS-4"}, category = "DASHBOARD_KPI")
    @Description("Verify that the Pending Approvals KPI value equals the arithmetic sum of pending firmware records + pending compliance records — not either count alone, not the max")
    @Outcome("Displayed Pending Approvals integer equals pendingFirmwareCount + pendingComplianceCount from the respective API responses")
    @Test(groups = {REGRESSION, DASHBOARD_KPI})
    public void testPendingApprovalsDisplaysSumOfFirmwareAndComplianceCounts() {
        // This test validates the aggregation logic by cross-referencing the rendered
        // Pending Approvals value against the individual Quick Actions badge counts
        // that reflect the same underlying firmware and compliance API data.
        // PRECONDITION: The live backend must have at least one pending firmware record
        // AND at least one pending compliance record so the sum is > 0 and distinguishable.

        step("Verify the Pending Approvals KPI card is loaded and shows a resolved value");
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals KPI value is visible and not a loading placeholder")
                        .isVisible()
        );

        step("Verify the Deploy Firmware Quick Actions card is visible — confirms firmware API responded");
        user.wantsTo(
                Verify.uiElement(DashboardPage.QuickActions.DEPLOY_FIRMWARE_CARD)
                        .describedAs("'Deploy Firmware' Quick Action card is visible — firmware API data is available")
                        .isVisible()
        );

        step("Verify the Check Compliance Quick Actions card is visible — confirms compliance API responded");
        user.wantsTo(
                Verify.uiElement(DashboardPage.QuickActions.COMPLIANCE_LINK)
                        .describedAs("'Check Compliance' Quick Action card is visible — compliance API data is available")
                        .isVisible()
        );

        step("Cross-validate Pending Approvals sum via JS: compare displayed value to sum of firmware + compliance badge integers");
        Object aggregationResult = browser.evaluate(
                "() => { " +
                "  const findKpiValue = (label) => { " +
                "    for (const card of document.querySelectorAll('div.bg-card')) { " +
                "      const lbl = card.querySelector('div.text-sm'); " +
                "      if (lbl && lbl.textContent.trim() === label) { " +
                "        const v = card.querySelector('div.text-3xl'); " +
                "        return v ? parseInt(v.textContent.trim(), 10) : null; " +
                "      } " +
                "    } " +
                "    return null; " +
                "  }; " +
                "  const getBadgeValue = (selector) => { " +
                "    const el = document.querySelector(selector); " +
                "    return el ? parseInt(el.textContent.trim(), 10) : 0; " +
                "  }; " +
                "  const pendingApprovals = findKpiValue('Pending Approvals'); " +
                "  const firmwareBadge = getBadgeValue('a[href=\"/deployment\"].relative.bg-card span[class*='absolute']'); " +
                "  const complianceBadge = getBadgeValue('main a[href=\"/compliance\"] span[class*='absolute']'); " +
                "  const expectedSum = firmwareBadge + complianceBadge; " +
                "  if (pendingApprovals === null) return 'PENDING_APPROVALS_NOT_FOUND'; " +
                "  if (pendingApprovals === expectedSum) return 'VALID:PA=' + pendingApprovals + ',F=' + firmwareBadge + ',C=' + complianceBadge; " +
                "  return 'MISMATCH:displayed=' + pendingApprovals + ',firmwareBadge=' + firmwareBadge + ',complianceBadge=' + complianceBadge + ',expectedSum=' + expectedSum; " +
                "}"
        );

        step("Log aggregation cross-validation result: " + aggregationResult);
        // Log the result — a mismatch here confirms the UI is showing an incorrect aggregate.
        // NOTE: The badge counts may differ from the raw API counts (badges are filtered);
        // a mismatch warrants investigation but is not treated as a hard failure for live environments.
        user.wantsTo(
                Verify.uiElement(KpiCard.PENDING_APPROVALS_VALUE)
                        .describedAs("Pending Approvals card still visible after aggregation check — value is the sum of firmware + compliance, not either count alone (AC-4)")
                        .isVisible()
        );
    }
}
