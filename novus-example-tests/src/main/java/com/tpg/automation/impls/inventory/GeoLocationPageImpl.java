package com.tpg.automation.impls.inventory;

import com.tpg.actions.Click;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.automation.pages.inventory.GeoLocationPage;
import com.tpg.automation.pages.inventory.InventoryPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Implementation layer for actions on the Geo Location tab of the Inventory module.
 *
 * <p>All methods return {@link Performable} instances to be executed via
 * {@code user.attemptsTo(...)}.  Follows the same pattern as
 * {@link DashboardPageImpl} and {@link InventoryPageImpl}.
 *
 * @jira PS-20 (Story: Replace Static Map in Inventory Module)
 * @jira PS-29 (QA Sub-task)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoLocationPageImpl {

    // ──────────────────────────── Tab Navigation ────────────────────────────

    /**
     * Clicks the "Geo Location" tab button on the Inventory page.
     * Requires the user to already be on the /inventory page.
     */
    public static Performable clickGeoLocationTab() {
        return Perform.actions(
                Click.on(InventoryPage.TAB_GEO)
        ).log("clickGeoLocationTab", "Clicks the 'Geo Location' tab button on the Inventory page");
    }

    /**
     * Clicks the "Hardware Inventory" tab button on the Inventory page.
     */
    public static Performable clickHardwareInventoryTab() {
        return Perform.actions(
                Click.on(InventoryPage.TAB_HARDWARE)
        ).log("clickHardwareInventoryTab", "Clicks the 'Hardware Inventory' tab button on the Inventory page");
    }

    /**
     * Clicks the "Firmware Status" tab button on the Inventory page.
     */
    public static Performable clickFirmwareStatusTab() {
        return Perform.actions(
                Click.on(InventoryPage.TAB_FIRMWARE)
        ).log("clickFirmwareStatusTab", "Clicks the 'Firmware Status' tab button on the Inventory page");
    }

    // ──────────────────────────── Filter Buttons ────────────────────────────

    /**
     * Clicks the "All" filter button to show all device pins on the map.
     */
    public static Performable clickFilterAll() {
        return Perform.actions(
                Click.on(GeoLocationPage.Filters.ALL)
        ).log("clickFilterAll", "Clicks the 'All' filter button on the Geo Location map view");
    }

    /**
     * Clicks the "Online(N)" filter button to show only Online device pins.
     */
    public static Performable clickFilterOnline() {
        return Perform.actions(
                Click.on(GeoLocationPage.Filters.ONLINE)
        ).log("clickFilterOnline", "Clicks the 'Online(N)' filter button to show only Online device pins");
    }

    /**
     * Clicks the "Offline(N)" filter button to show only Offline device pins.
     */
    public static Performable clickFilterOffline() {
        return Perform.actions(
                Click.on(GeoLocationPage.Filters.OFFLINE)
        ).log("clickFilterOffline", "Clicks the 'Offline(N)' filter button to show only Offline device pins");
    }

    /**
     * Clicks the "Maintenance(N)" filter button to show only Maintenance device pins.
     */
    public static Performable clickFilterMaintenance() {
        return Perform.actions(
                Click.on(GeoLocationPage.Filters.MAINTENANCE)
        ).log("clickFilterMaintenance", "Clicks the 'Maintenance(N)' filter button to show only Maintenance device pins");
    }

    // ──────────────────────────── Map Markers ────────────────────────────────

    /**
     * Force-clicks the first visible map marker pin.
     * Force-click is required as map markers are positioned over the canvas element
     * and may be considered obscured by the standard click check.
     */
    public static Performable clickFirstMapMarker() {
        return Perform.actions(
                Click.on(GeoLocationPage.MAP_MARKER)
        ).log("clickFirstMapMarker", "Force-clicks the first visible Map marker pin to open the device detail card");
    }

    // ──────────────────────────── Device Detail Card ─────────────────────────

    /**
     * Clicks the X (Close) button to dismiss the device detail card.
     */
    public static Performable closeDetailCard() {
        return Perform.actions(
                Click.on(GeoLocationPage.DetailCard.CLOSE_BUTTON)
        ).log("closeDetailCard", "Clicks the Close (X) button to dismiss the device detail card");
    }

    // ──────────────────────────── Map Controls ────────────────────────────────

    /**
     * Clicks the Zoom In (+) button on the MapLibre control panel.
     */
    public static Performable clickZoomIn() {
        return Perform.actions(
                Click.on(GeoLocationPage.Controls.ZOOM_IN)
        ).log("clickZoomIn", "Clicks the Zoom In (+) button on the MapLibre map controls");
    }

    /**
     * Clicks the Zoom Out (-) button on the MapLibre control panel.
     */
    public static Performable clickZoomOut() {
        return Perform.actions(
                Click.on(GeoLocationPage.Controls.ZOOM_OUT)
        ).log("clickZoomOut", "Clicks the Zoom Out (-) button on the MapLibre map controls");
    }

    /**
     * Clicks the Compass / north-reset button on the MapLibre control panel.
     */
    public static Performable clickCompass() {
        return Perform.actions(
                Click.on(GeoLocationPage.Controls.COMPASS)
        ).log("clickCompass", "Clicks the Compass/North-reset button on the MapLibre map controls");
    }
}
