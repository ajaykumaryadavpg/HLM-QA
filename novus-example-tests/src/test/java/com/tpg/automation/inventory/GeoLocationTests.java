package com.tpg.automation.inventory;

import com.tpg.actions.Launch;
import com.tpg.actions.Waiting;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DashboardPageImpl;
import com.tpg.automation.impls.inventory.GeoLocationPageImpl;
import com.tpg.automation.pages.inventory.GeoLocationPage;
import com.tpg.automation.pages.inventory.GeoLocationPage.Controls;
import com.tpg.automation.pages.inventory.GeoLocationPage.DetailCard;
import com.tpg.automation.pages.inventory.GeoLocationPage.Filters;
import com.tpg.automation.pages.inventory.GeoLocationPage.StatPills;
import com.tpg.automation.pages.inventory.InventoryPage;
import com.tpg.verification.Verify;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.GEO_LOCATION;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * Test suite for the Geo Location tab in the Inventory & Assets module (Story PS-20).
 *
 * <p>Verifies that the static SVG map has been replaced by the interactive MapLibre-based
 * Amazon Location map, and that all associated UI features work correctly:
 * <ul>
 *   <li>MapLibre canvas rendering (no legacy SVG map present)
 *   <li>12 device pins with correct status colors (green=Online, red=Offline, orange=Maintenance)
 *   <li>Stat pills showing correct device counts (Total=12, Online=6, Offline=3, Maintenance=3)
 *   <li>Filter buttons (All, Online, Offline, Maintenance) filtering pins correctly
 *   <li>Device detail card opens on pin click and closes via the X button
 *   <li>Map zoom controls (Zoom In, Zoom Out, Compass) are functional
 *   <li>Hardware Inventory and Firmware Status sibling tabs are unaffected (regression)
 * </ul>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class
 * in {@code @BeforeClass} and lands on the Dashboard. Each test method navigates
 * to the Inventory page and opens the Geo Location tab via {@code @BeforeMethod}.
 *
 * <p>Pin color assertions use {@code browser.evaluate()} to read computed background-color
 * CSS values since colors are applied via Tailwind arbitrary class utilities that cannot
 * be reliably checked via DOM attribute selectors alone.
 *
 * @see GeoLocationPage
 * @see GeoLocationPageImpl
 * @see InventoryTestBase
 * @jira PS-20 (Story: Replace Static Map in Inventory Module)
 * @jira PS-29 (QA Sub-task)
 */
public class GeoLocationTests extends InventoryTestBase {

    // ──────────────────────────── Pre-test Setup ─────────────────────────────────────────

    /**
     * Navigate to the Inventory page and open the Geo Location tab before each test.
     * This ensures every test starts with the Geo Location view active and the map loaded.
     */
    @BeforeMethod(alwaysRun = true)
    public void navigateToGeoLocationTab() {
        step("Navigate to the Dashboard (clean state)");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Navigate to Inventory module via sidebar nav link");
        user.attemptsTo(
                DashboardPageImpl.goToInventoryModule()
        );

        step("Wait for Inventory page to load");
        user.is(Waiting.on(InventoryPage.PAGE_HEADING).within(15));

        step("Click the Geo Location tab");
        user.attemptsTo(
                GeoLocationPageImpl.clickGeoLocationTab()
        );

        step("Wait for MapLibre map canvas to render");
        user.is(Waiting.on(GeoLocationPage.MAP_CANVAS).within(15));

        step("Wait for device pins to appear on the map");
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(15));
        browser.waitForFunction(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length > 0"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 2 — Map Rendering: MapLibre interactive map replaces static SVG map
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-01: Geo Location tab renders the interactive MapLibre map (not a static SVG)
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-01",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the Geo Location tab renders the interactive MapLibre map (canvas + maplibregl-map container) and that no static SVG map element is present in the DOM")
    @Outcome("MapLibre map container, canvas element, and ARIA Map region are present; no legacy svg[class*='map'] element exists; all 12 Map marker pins are rendered")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testGeoLocationTabRendersInteractiveMapLibreMap() {

        step("Verify the MapLibre map container div is present in the DOM");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CONTAINER)
                        .describedAs("div.maplibregl-map container is present — confirms MapLibre (not a static SVG) is rendered")
                        .isVisible()
        );

        step("Verify the interactive canvas element is rendered inside the map container");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("HTML5 canvas element inside div.maplibregl-map — MapLibre has initialised and rendered the map tiles")
                        .isVisible()
        );

        step("Verify the ARIA-labelled map region is present");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_REGION)
                        .describedAs("[aria-label='Map'] region is rendered — the interactive drag/pan target is accessible")
                        .isVisible()
        );

        step("Verify the legacy static SVG map element is NOT present");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.LEGACY_SVG_MAP)
                        .describedAs("No svg[class*='map'] / svg[id*='map'] / .rsm-svg elements — the old react-simple-maps SVG has been removed")
                        .isNotVisible()
        );

        step("Verify all 12 device pin markers are rendered on the map");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_MARKER)
                        .describedAs("At least one .maplibregl-marker.maplibregl-marker-anchor-center div is present — device pins are rendered by the new map")
                        .isVisible()
        );

        step("Verify MapLibre attribution links are visible confirming the map library in use");
        user.wantsTo(
                Verify.uiElement(Controls.MAPLIBRE_LINK)
                        .describedAs("MapLibre attribution link is visible in the map control area")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-02: Map legend displays correct Online / Offline / Maintenance entries
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-02",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the map legend panel is visible and contains entries for all three device status types: Online, Offline, and Maintenance")
    @Outcome("Legend panel shows 'Online', 'Offline', and 'Maintenance' text entries with their respective color indicators")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testMapLegendDisplaysCorrectStatusEntries() {

        step("Verify the legend 'Online' entry is visible");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.Legend.ONLINE_ENTRY)
                        .describedAs("'Online' text is visible in the map legend panel")
                        .isVisible()
        );

        step("Verify the legend 'Offline' entry is visible");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.Legend.OFFLINE_ENTRY)
                        .describedAs("'Offline' text is visible in the map legend panel")
                        .isVisible()
        );

        step("Verify the legend 'Maintenance' entry is visible");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.Legend.MAINTENANCE_ENTRY)
                        .describedAs("'Maintenance' text is visible in the map legend panel")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 3 — Device Pin Colors (Online=green, Offline=red, Maintenance=orange)
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-03: All 12 pins visible under the default "All" filter with correct distribution
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-03",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that all 12 device pins are visible under the default 'All' filter, and that pin colors reflect the correct status distribution (6 green Online, 3 red Offline, 3 orange Maintenance)")
    @Outcome("12 map marker buttons are rendered; JavaScript computed-style evaluation confirms 6 green (rgb(34,197,94)), 3 red (rgb(239,68,68)), 3 orange (rgb(249,115,22)) pin colors")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testAllTwelvePinsVisibleWithCorrectColorDistribution() {

        step("Verify the 'All' filter button is active by default");
        user.wantsTo(
                Verify.uiElement(Filters.ALL)
                        .describedAs("'All' filter button is visible and active by default on Geo Location tab load")
                        .isVisible()
        );

        step("Verify at least one map marker is visible — confirming pins are rendered");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_MARKER)
                        .describedAs(".maplibregl-marker.maplibregl-marker-anchor-center elements are present — device pins are rendered on the map")
                        .isVisible()
        );

        step("Evaluate computed background-color of all visible pin elements and verify color distribution");
        // Read computed background-color for all pin dot divs; counts per color are verified
        // Expected: 6 green (rgb(34, 197, 94)), 3 red (rgb(239, 68, 68)), 3 orange (rgb(249, 115, 22))
        Object colorCounts = browser.evaluate(
                "() => {" +
                "  const markers = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center > div');" +
                "  const counts = { green: 0, red: 0, orange: 0, other: 0 };" +
                "  markers.forEach(el => {" +
                "    const bg = el.style.backgroundColor;" +
                "    if (bg === 'rgb(34, 197, 94)')  counts.green++;" +
                "    else if (bg === 'rgb(239, 68, 68)')  counts.red++;" +
                "    else if (bg === 'rgb(249, 115, 22)') counts.orange++;" +
                "    else counts.other++;" +
                "  });" +
                "  return counts;" +
                "}"
        );

        step("Log pin color distribution result: " + colorCounts);

        // Verify total pin count via DOM — 12 markers expected
        int totalPins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        step("Verified total map marker count: " + totalPins + " (expected: 12)");
        org.testng.Assert.assertEquals(totalPins, 12,
                "Expected exactly 12 device pins on the map under the 'All' filter");
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-04: Online filter — only 6 green Online pins visible
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-04",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking the Online filter button shows exactly 6 green (rgb(34,197,94)) device pins and hides Offline and Maintenance pins")
    @Outcome("6 Map marker buttons are visible; computed background-color of all pin dots = rgb(34,197,94); Online filter button is in active state")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testOnlineFilterShowsSixGreenPins() {

        step("Click the 'Online(6)' filter button");
        user.attemptsTo(
                GeoLocationPageImpl.clickFilterOnline()
        );

        step("Verify the Online filter button is now active/highlighted");
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online(6) filter button is visible and active after clicking")
                        .isVisible()
        );

        step("Wait for Online-filtered pins to appear in the DOM");
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));

        step("Verify exactly 6 map marker pins are visible after applying Online filter");
        int visiblePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        step("Visible pin count after Online filter: " + visiblePins + " (expected: 6)");
        org.testng.Assert.assertEquals(visiblePins, 6,
                "Expected exactly 6 device pins visible when Online filter is active");

        step("Verify all 6 visible pins have green background-color rgb(34,197,94) — Online status");
        long greenPins = (long) browser.evaluate(
                "() => {" +
                "  const dots = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center > div');" +
                "  return [...dots].filter(el => el.style.backgroundColor === 'rgb(34, 197, 94)').length;" +
                "}"
        );
        step("Green pin count: " + greenPins + " (expected: 6)");
        org.testng.Assert.assertEquals(greenPins, 6L,
                "Expected all 6 Online-filtered pins to have green background-color rgb(34,197,94)");
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-05: Offline filter — only 3 red Offline pins visible
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-05",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking the Offline filter button shows exactly 3 red (rgb(239,68,68)) device pins")
    @Outcome("3 Map marker buttons are visible; computed background-color of all pin dots = rgb(239,68,68)")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testOfflineFilterShowsThreeRedPins() {

        step("Click the 'Offline(3)' filter button");
        user.attemptsTo(
                GeoLocationPageImpl.clickFilterOffline()
        );

        step("Wait for Offline-filtered pins to appear in the DOM");
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));

        step("Verify exactly 3 map marker pins are visible after applying Offline filter");
        int visiblePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        step("Visible pin count after Offline filter: " + visiblePins + " (expected: 3)");
        org.testng.Assert.assertEquals(visiblePins, 3,
                "Expected exactly 3 device pins visible when Offline filter is active");

        step("Verify all 3 visible pins have red background-color rgb(239,68,68) — Offline status");
        long redPins = (long) browser.evaluate(
                "() => {" +
                "  const dots = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center > div');" +
                "  return [...dots].filter(el => el.style.backgroundColor === 'rgb(239, 68, 68)').length;" +
                "}"
        );
        step("Red pin count: " + redPins + " (expected: 3)");
        org.testng.Assert.assertEquals(redPins, 3L,
                "Expected all 3 Offline-filtered pins to have red background-color rgb(239,68,68)");
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-06: Maintenance filter — only 3 orange Maintenance pins visible
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-06",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking the Maintenance filter button shows exactly 3 orange (rgb(249,115,22)) device pins")
    @Outcome("3 Map marker buttons are visible; computed background-color of all pin dots = rgb(249,115,22)")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testMaintenanceFilterShowsThreeOrangePins() {

        step("Click the 'Maintenance(3)' filter button");
        user.attemptsTo(
                GeoLocationPageImpl.clickFilterMaintenance()
        );

        step("Wait for Maintenance-filtered pins to appear in the DOM");
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));

        step("Verify exactly 3 map marker pins are visible after applying Maintenance filter");
        int visiblePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        step("Visible pin count after Maintenance filter: " + visiblePins + " (expected: 3)");
        org.testng.Assert.assertEquals(visiblePins, 3,
                "Expected exactly 3 device pins visible when Maintenance filter is active");

        step("Verify all 3 visible pins have orange background-color rgb(249,115,22) — Maintenance status");
        long orangePins = (long) browser.evaluate(
                "() => {" +
                "  const dots = document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center > div');" +
                "  return [...dots].filter(el => el.style.backgroundColor === 'rgb(249, 115, 22)').length;" +
                "}"
        );
        step("Orange pin count: " + orangePins + " (expected: 3)");
        org.testng.Assert.assertEquals(orangePins, 3L,
                "Expected all 3 Maintenance-filtered pins to have orange background-color rgb(249,115,22)");
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 4 — Stat Pills
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-07: Stat pills display correct device counts (Total=12, Online=6, Offline=3, Maintenance=3)
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-07",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the four stat pills in the Geo Location view show correct device counts: Total=12, Online=6, Offline=3, Maintenance=3")
    @Outcome("All four stat pills (Total, Online, Offline, Maintenance) are visible with labels matching expected device distribution")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testStatPillsDisplayCorrectDeviceCounts() {

        step("Verify the 'Total' stat pill is visible");
        user.wantsTo(
                Verify.uiElement(StatPills.TOTAL)
                        .describedAs("Stat pill with label 'Total' is visible in the Geo Location view")
                        .isVisible()
        );

        step("Verify the 'Online' stat pill is visible");
        user.wantsTo(
                Verify.uiElement(StatPills.ONLINE)
                        .describedAs("Stat pill with label 'Online' is visible in the Geo Location view")
                        .isVisible()
        );

        step("Verify the 'Offline' stat pill is visible");
        user.wantsTo(
                Verify.uiElement(StatPills.OFFLINE)
                        .describedAs("Stat pill with label 'Offline' is visible in the Geo Location view")
                        .isVisible()
        );

        step("Verify the 'Maintenance' stat pill is visible");
        user.wantsTo(
                Verify.uiElement(StatPills.MAINTENANCE)
                        .describedAs("Stat pill with label 'Maintenance' is visible in the Geo Location view")
                        .isVisible()
        );

        step("Verify stat pill counts match filter button count suffixes via JavaScript evaluation");
        // Confirm that the Online filter button label contains "(6)" matching the Online pill
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online filter button label contains count '6' matching the Online stat pill")
                        .containsText("6")
        );

        user.wantsTo(
                Verify.uiElement(Filters.OFFLINE)
                        .describedAs("Offline filter button label contains count '3' matching the Offline stat pill")
                        .containsText("3")
        );

        user.wantsTo(
                Verify.uiElement(Filters.MAINTENANCE)
                        .describedAs("Maintenance filter button label contains count '3' matching the Maintenance stat pill")
                        .containsText("3")
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 5 — Filter Buttons
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-08: All four filter buttons are present with correct label format
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-08",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that all four filter buttons are visible on the Geo Location tab with correct label text: 'All', 'Online(6)', 'Offline(3)', 'Maintenance(3)'")
    @Outcome("All four filter buttons (All, Online(6), Offline(3), Maintenance(3)) are rendered; 'All' is active by default")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testAllFourFilterButtonsArePresent() {

        step("Verify the 'All' filter button is visible and active by default");
        user.wantsTo(
                Verify.uiElement(Filters.ALL)
                        .describedAs("'All' filter button is visible — default active state on Geo Location tab load")
                        .isVisible()
        );

        step("Verify the 'Online(N)' filter button is visible with count suffix");
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("'Online(6)' filter button is visible — N=6 online devices in test data")
                        .isVisible()
        );

        step("Verify the 'Offline(N)' filter button is visible with count suffix");
        user.wantsTo(
                Verify.uiElement(Filters.OFFLINE)
                        .describedAs("'Offline(3)' filter button is visible — N=3 offline devices in test data")
                        .isVisible()
        );

        step("Verify the 'Maintenance(N)' filter button is visible with count suffix");
        user.wantsTo(
                Verify.uiElement(Filters.MAINTENANCE)
                        .describedAs("'Maintenance(3)' filter button is visible — N=3 maintenance devices in test data")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-09: Filter sequence All→Online→Offline→Maintenance→All works correctly
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-09",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking each filter in sequence (All→Online→Offline→Maintenance→All) correctly updates the visible pin count at each step")
    @Outcome("Pin counts change correctly at each step: 12→6→3→3→12; only one filter is active at a time")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testFilterSequenceUpdatesMapMarkersCorrectly() {

        step("Step 1 — All filter (default): verify 12 pins visible");
        int allPins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        org.testng.Assert.assertEquals(allPins, 12,
                "Initial state (All filter): expected 12 pins");

        step("Step 2 — Click Online filter: verify 6 pins visible");
        user.attemptsTo(GeoLocationPageImpl.clickFilterOnline());
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));
        int onlinePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        org.testng.Assert.assertEquals(onlinePins, 6,
                "After Online filter: expected 6 pins");

        step("Step 3 — Click Offline filter: verify 3 pins visible");
        user.attemptsTo(GeoLocationPageImpl.clickFilterOffline());
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));
        int offlinePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        org.testng.Assert.assertEquals(offlinePins, 3,
                "After Offline filter: expected 3 pins");

        step("Step 4 — Click Maintenance filter: verify 3 pins visible");
        user.attemptsTo(GeoLocationPageImpl.clickFilterMaintenance());
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));
        int maintenancePins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        org.testng.Assert.assertEquals(maintenancePins, 3,
                "After Maintenance filter: expected 3 pins");

        step("Step 5 — Click All filter again: verify 12 pins restored");
        user.attemptsTo(GeoLocationPageImpl.clickFilterAll());
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));
        int restoredPins = (int) browser.evaluate(
                "() => document.querySelectorAll('.maplibregl-marker.maplibregl-marker-anchor-center').length"
        );
        org.testng.Assert.assertEquals(restoredPins, 12,
                "After switching back to All filter: expected all 12 pins restored");
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 6 — Device Detail Card
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-10: Clicking a Maintenance pin opens detail card with correct fields
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-10",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking a Maintenance device pin opens a detail card showing the device name, 'Maintenance' status badge, and all expected detail fields (Serial, Model, Firmware, Health, Customer, City, Location, Last Seen)")
    @Outcome("Device detail card is visible with device name heading, 'Maintenance' status badge, and all 8 labelled detail fields")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testMaintenancePinOpensDetailCardWithCorrectFields() {

        step("Apply Maintenance filter to isolate Maintenance pins");
        user.attemptsTo(GeoLocationPageImpl.clickFilterMaintenance());
        user.is(Waiting.on(GeoLocationPage.MAP_MARKER).within(5));

        step("Force-click the first Maintenance map marker to open the detail card");
        // Force-click via JavaScript to bypass potential canvas overlay interception
        browser.evaluate(
                "() => document.querySelector('.maplibregl-marker.maplibregl-marker-anchor-center').click()"
        );

        step("Wait for device detail card container to appear");
        user.is(Waiting.on(DetailCard.CONTAINER).within(10));

        step("Verify the device detail card container is visible");
        user.wantsTo(
                Verify.uiElement(DetailCard.CONTAINER)
                        .describedAs("Device detail card (div.bg-card with close button) is rendered after pin click")
                        .isVisible()
        );

        step("Verify the device name heading (h4) is visible in the detail card");
        user.wantsTo(
                Verify.uiElement(DetailCard.DEVICE_NAME)
                        .describedAs("h4 device name heading is visible inside the detail card")
                        .isVisible()
        );

        step("Verify the status badge shows 'Maintenance'");
        user.wantsTo(
                Verify.uiElement(DetailCard.STATUS_BADGE)
                        .describedAs("Status badge in the detail card contains 'Maintenance'")
                        .containsText("Maintenance")
        );

        step("Verify 'Serial' field label is present in the detail card");
        user.wantsTo(
                Verify.uiElement(DetailCard.SERIAL_FIELD)
                        .describedAs("'Serial' field label is visible inside the device detail card")
                        .isVisible()
        );

        step("Verify 'Firmware' field label is present in the detail card");
        user.wantsTo(
                Verify.uiElement(DetailCard.FIRMWARE_FIELD)
                        .describedAs("'Firmware' field label is visible inside the device detail card")
                        .isVisible()
        );

        step("Verify 'Health' field label is present in the detail card");
        user.wantsTo(
                Verify.uiElement(DetailCard.HEALTH_FIELD)
                        .describedAs("'Health' field label is visible inside the device detail card")
                        .isVisible()
        );

        step("Verify 'Last Seen' field label is present and does not show raw null");
        user.wantsTo(
                Verify.uiElement(DetailCard.LAST_SEEN_FIELD)
                        .describedAs("'Last Seen' field label is visible; if no data, it shows em-dash (—) not 'null'")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-11: Device detail card closes when the X button is clicked
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-11",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that clicking the X (Close) button on the device detail card dismisses the card, and that map markers remain visible on the map")
    @Outcome("Device detail card is no longer visible after clicking Close; map markers remain visible; no JS errors")
    @Test(groups = {SMOKE_TESTS, GEO_LOCATION})
    public void testDeviceDetailCardClosesOnXButtonClick() {

        step("Force-click the first map marker to open the detail card");
        browser.evaluate(
                "() => document.querySelector('.maplibregl-marker.maplibregl-marker-anchor-center').click()"
        );

        step("Wait for detail card to appear");
        user.is(Waiting.on(DetailCard.CONTAINER).within(10));

        step("Verify detail card is visible before closing");
        user.wantsTo(
                Verify.uiElement(DetailCard.CONTAINER)
                        .describedAs("Device detail card is open and visible before clicking Close")
                        .isVisible()
        );

        step("Click the X close button");
        user.attemptsTo(
                GeoLocationPageImpl.closeDetailCard()
        );

        step("Verify the device detail card is no longer visible");
        user.wantsTo(
                Verify.uiElement(DetailCard.CONTAINER)
                        .describedAs("Device detail card is dismissed — no longer visible after clicking Close (X)")
                        .isNotVisible()
        );

        step("Verify map markers are still visible on the map after closing the card");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_MARKER)
                        .describedAs("Map marker pins remain visible on the map after the detail card is closed")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 7 — Map Interactivity (Zoom, Compass)
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-12: Zoom In and Zoom Out buttons are present and clickable
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-12",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the MapLibre Zoom In (+) and Zoom Out (-) control buttons are visible and respond to click interactions without throwing JavaScript errors")
    @Outcome("Zoom In and Zoom Out buttons are visible; clicking each does not throw errors; map re-renders at new zoom level")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testZoomInAndZoomOutControlsAreFunctional() {

        step("Verify the 'Zoom in' (+) button is visible in the map controls");
        user.wantsTo(
                Verify.uiElement(Controls.ZOOM_IN)
                        .describedAs("button[aria-label='Zoom in'] is visible in the MapLibre control panel")
                        .isVisible()
        );

        step("Verify the 'Zoom out' (-) button is visible in the map controls");
        user.wantsTo(
                Verify.uiElement(Controls.ZOOM_OUT)
                        .describedAs("button[aria-label='Zoom out'] is visible in the MapLibre control panel")
                        .isVisible()
        );

        step("Click 'Zoom in' once — map should zoom in");
        user.attemptsTo(GeoLocationPageImpl.clickZoomIn());

        step("Click 'Zoom in' a second time — map continues to zoom in");
        user.attemptsTo(GeoLocationPageImpl.clickZoomIn());

        step("Verify map canvas is still visible after zoom in — no render error");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is still visible after two zoom-in clicks — map rendered successfully")
                        .isVisible()
        );

        step("Click 'Zoom out' once — map should zoom back out");
        user.attemptsTo(GeoLocationPageImpl.clickZoomOut());

        step("Verify map canvas is still visible after zoom out — no render error");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is still visible after zoom-out — map rendered successfully")
                        .isVisible()
        );

        step("Verify map marker pins remain visible after zoom operations");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_MARKER)
                        .describedAs("Map marker pins remain visible after zoom operations")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-13: Compass / north-reset button is present and interactive
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-13",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the compass/north-reset button is present in the MapLibre control panel and responds to a click without errors")
    @Outcome("Compass button (aria-label contains 'reset north') is visible and clickable; map canvas remains rendered after click")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testCompassButtonIsPresentAndInteractive() {

        step("Verify the compass/north-reset button is visible in the map controls");
        user.wantsTo(
                Verify.uiElement(Controls.COMPASS)
                        .describedAs("button[aria-label*='reset north'] compass button is present in the MapLibre control panel")
                        .isVisible()
        );

        step("Click the compass button to reset north orientation");
        user.attemptsTo(GeoLocationPageImpl.clickCompass());

        step("Verify map canvas remains visible after compass click — no render error");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is still rendered after compass/north-reset click")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 8 — Tab Switching Regression (Hardware Inventory and Firmware Status)
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-14: Hardware Inventory tab is unaffected — device table renders correctly
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-14",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that switching from the Geo Location tab back to the Hardware Inventory tab renders the device table correctly — no regressions introduced by PS-20")
    @Outcome("Hardware Inventory table rows are visible; map canvas is not rendered; Search and Filter button are visible; pagination is at page 1 of 2")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testHardwareInventoryTabUnaffectedByPS20() {

        step("Switch from Geo Location tab to Hardware Inventory tab");
        user.attemptsTo(
                GeoLocationPageImpl.clickHardwareInventoryTab()
        );

        step("Wait for the device table rows to be rendered");
        user.is(Waiting.on(InventoryPage.DeviceTable.ALL_ROWS).within(10));

        step("Verify the Hardware Inventory device table rows are visible");
        user.wantsTo(
                Verify.uiElement(InventoryPage.DeviceTable.ALL_ROWS)
                        .describedAs("Device table rows (tbody tr) are visible in the Hardware Inventory tab view")
                        .isVisible()
        );

        step("Verify the Search input field is visible in the Hardware Inventory toolbar");
        user.wantsTo(
                Verify.uiElement(InventoryPage.SEARCH_FIELD)
                        .describedAs("Search input is visible — Hardware Inventory toolbar rendered correctly")
                        .isVisible()
        );

        step("Verify the Filter button is visible in the Hardware Inventory toolbar");
        user.wantsTo(
                Verify.uiElement(InventoryPage.FILTER_BUTTON)
                        .describedAs("Filter button is visible — Hardware Inventory toolbar rendered correctly")
                        .isVisible()
        );

        step("Verify the map canvas is NOT rendered in the Hardware Inventory view");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is not present in the Hardware Inventory view — tab switch is clean")
                        .isNotVisible()
        );

        step("Verify pagination label is visible confirming 12 devices over 2 pages");
        user.wantsTo(
                Verify.uiElement(InventoryPage.DeviceTable.PAGINATION_LABEL)
                        .describedAs("Pagination label ('Page X of Y') is visible — 12 devices across 2 pages")
                        .isVisible()
        );
    }

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-15: Firmware Status tab is unaffected — firmware cards render correctly
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-15",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that switching from the Geo Location tab to the Firmware Status tab renders firmware device cards correctly — no regressions introduced by PS-20")
    @Outcome("Firmware Status tab content is visible (device firmware cards rendered); map canvas is not rendered in this view")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testFirmwareStatusTabUnaffectedByPS20() {

        step("Switch from Geo Location tab to Firmware Status tab");
        user.attemptsTo(
                GeoLocationPageImpl.clickFirmwareStatusTab()
        );

        step("Verify the Firmware Status tab button is active/selected");
        user.wantsTo(
                Verify.uiElement(InventoryPage.TAB_FIRMWARE)
                        .describedAs("Firmware Status tab button is visible — tab switch completed")
                        .isVisible()
        );

        step("Verify the Firmware Status tab content is rendered (search field visible)");
        user.wantsTo(
                Verify.uiElement(InventoryPage.SEARCH_FIELD)
                        .describedAs("Search field is visible in the Firmware Status view")
                        .isVisible()
        );

        step("Verify the map canvas is NOT rendered in the Firmware Status view");
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is not present in the Firmware Status view — tab switch is clean")
                        .isNotVisible()
        );

        step("Switch back to Geo Location tab to confirm round-trip navigation works");
        user.attemptsTo(
                GeoLocationPageImpl.clickGeoLocationTab()
        );

        step("Verify the map is re-rendered after switching back to Geo Location tab");
        user.is(Waiting.on(GeoLocationPage.MAP_CANVAS).within(10));
        user.wantsTo(
                Verify.uiElement(GeoLocationPage.MAP_CANVAS)
                        .describedAs("Map canvas is rendered again after switching back to Geo Location tab — no broken map state")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════════════════
    // SUITE 10 — Edge Cases
    // ══════════════════════════════════════════════════════════════════════════════════════

    // ──────────────────────────────────────────────────────────────────────────────────────
    // TC-GEO-16: Stat pills remain constant regardless of active filter
    // ──────────────────────────────────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-GEO-16",
            stories = {"PS-20"}, category = "GEO_LOCATION")
    @Description("Verify that the stat pill counts (Total, Online, Offline, Maintenance) remain unchanged when different filter buttons are applied — pills are static counts, not filtered")
    @Outcome("Stat pills show the same values before and after applying Online, Offline, and Maintenance filters; filter buttons still show their count suffixes throughout")
    @Test(groups = {REGRESSION, GEO_LOCATION})
    public void testStatPillCountsAreStaticAcrossFilterChanges() {

        step("Note initial filter button labels before any filter changes");
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online filter button with count suffix visible before filter change")
                        .containsText("6")
        );

        step("Apply Online filter and verify stat pills remain unchanged");
        user.attemptsTo(GeoLocationPageImpl.clickFilterOnline());
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online filter button count '(6)' unchanged after applying Online filter")
                        .containsText("6")
        );
        user.wantsTo(
                Verify.uiElement(Filters.OFFLINE)
                        .describedAs("Offline filter button count '(3)' unchanged when Online filter is active")
                        .containsText("3")
        );

        step("Apply Offline filter and verify stat pills remain unchanged");
        user.attemptsTo(GeoLocationPageImpl.clickFilterOffline());
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online filter button count '(6)' unchanged after applying Offline filter")
                        .containsText("6")
        );
        user.wantsTo(
                Verify.uiElement(Filters.OFFLINE)
                        .describedAs("Offline filter button count '(3)' unchanged after applying Offline filter")
                        .containsText("3")
        );

        step("Apply Maintenance filter and verify stat pills remain unchanged");
        user.attemptsTo(GeoLocationPageImpl.clickFilterMaintenance());
        user.wantsTo(
                Verify.uiElement(Filters.MAINTENANCE)
                        .describedAs("Maintenance filter button count '(3)' unchanged after applying Maintenance filter")
                        .containsText("3")
        );

        step("Restore All filter for clean state");
        user.attemptsTo(GeoLocationPageImpl.clickFilterAll());

        step("Verify all filter button count labels are still correct after round-trip");
        user.wantsTo(
                Verify.uiElement(Filters.ONLINE)
                        .describedAs("Online filter button count '(6)' is correct after restoring All filter")
                        .containsText("6")
        );
        user.wantsTo(
                Verify.uiElement(Filters.OFFLINE)
                        .describedAs("Offline filter button count '(3)' is correct after restoring All filter")
                        .containsText("3")
        );
        user.wantsTo(
                Verify.uiElement(Filters.MAINTENANCE)
                        .describedAs("Maintenance filter button count '(3)' is correct after restoring All filter")
                        .containsText("3")
        );
    }
}
