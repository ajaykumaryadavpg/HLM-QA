package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the Geo Location tab within the Inventory & Assets module.
 *
 * Locators verified via live browser DOM inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/inventory (Geo Location tab)
 *
 * Key findings (PS-20 — MapLibre interactive map replaces static SVG map):
 *  - Map renders inside div.maplibregl-map; the interactive canvas is a child canvas element
 *  - Map region aria: [aria-label='Map'] — used as the stable drag/pan target
 *  - Device pins: button[aria-label='Map marker'] — 12 total (6 Online, 3 Offline, 3 Maintenance)
 *  - Pin colors (background-color CSS computed): green=rgb(34,197,94) Online,
 *    red=rgb(239,68,68) Offline, orange=rgb(249,115,22) Maintenance
 *  - Stat pills sit above the filter bar; each pill is a div.bg-card (or similar) containing
 *    a numeric value and a label text
 *  - Filter buttons: "All", "Online(N)", "Offline(N)", "Maintenance(N)" — button elements
 *  - Active filter button carries bg-[#2563eb] and text-white Tailwind classes
 *  - Device detail card appears on pin click — contains h4 device name, status badge,
 *    and labelled detail rows; closed via a button with aria-label='Close'
 *  - Zoom controls: button[aria-label='Zoom in'] / button[aria-label='Zoom out']
 *  - Compass/north reset: button[aria-label*='reset north']
 *  - Attribution: .maplibregl-ctrl-attrib-inner links to MapLibre and OpenStreetMap
 *  - NO legacy svg[class*='map'] or svg[id*='map'] elements are present (static map removed)
 *
 * @jira PS-20 (Story: Replace Static Map in Inventory Module)
 * @jira PS-29 (QA Sub-task)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GeoLocationPage {

    // ──────────────────────────── Map Container ────────────────────────────

    /**
     * Outer MapLibre map wrapper div.
     * Presence confirms the interactive map (not a static SVG) is rendered.
     */
    public static final String MAP_CONTAINER =
            LocateBy.css("div.maplibregl-map");

    /**
     * Interactive map canvas element (MapLibre renders to HTML5 canvas).
     * Presence of at least one canvas inside the map container confirms MapLibre has initialised.
     */
    public static final String MAP_CANVAS =
            LocateBy.css("div.maplibregl-map canvas");

    /**
     * ARIA-labelled map region — the primary drag/pan interaction target.
     * Stable across MapLibre versions; used for pan (drag) test steps.
     */
    public static final String MAP_REGION =
            LocateBy.css("[aria-label='Map']");

    /**
     * Static SVG map selector — must NOT be present after PS-20 is deployed.
     * Used in negative assertions to confirm the legacy map has been removed.
     * Matches svg elements whose class or id contains 'map' (react-simple-maps convention).
     */
    public static final String LEGACY_SVG_MAP =
            LocateBy.css("svg[class*='map'], svg[id*='map'], .rsm-svg");

    // ──────────────────────────── Device Pins (Map Markers) ────────────────────────────

    /**
     * All map marker pin elements currently visible on the map.
     *
     * DOM reality (verified via live browser inspection — PS-29 healer run 2026-03-23):
     *  - Pins are rendered as <div role="button" aria-label="Map marker" …> by MapLibre,
     *    NOT as <button> HTML elements.  A CSS selector of "button[aria-label='Map marker']"
     *    returns 0 results because no <button> tag exists.
     *  - The stable selector is the MapLibre CSS class pair:
     *    .maplibregl-marker.maplibregl-marker-anchor-center
     *  - Pins are physically REMOVED from the DOM when a status filter is applied
     *    (not hidden with display:none / visibility:hidden).  Counts via
     *    querySelectorAll reflect the filtered set directly.
     *
     * Count = total visible after applying the current filter (All=12, Online=6, Offline=3, Maintenance=3).
     */
    public static final String MAP_MARKER =
            LocateBy.css(".maplibregl-marker.maplibregl-marker-anchor-center");

    /**
     * The inner div of a map marker — carries the inline background-color style
     * used for pin color assertions (green / red / orange).
     *
     * DOM reality: the inner dot has NO CSS class — only inline style attributes.
     *   width: 10px; height: 10px; border-radius: 50%; background-color: rgb(…)
     * Selected state: grows to 16×16px with 2px border.
     */
    public static final String MAP_MARKER_DOT =
            LocateBy.css(".maplibregl-marker.maplibregl-marker-anchor-center > div");

    // ──────────────────────────── Summary / Stat Pills ─────────────────────────────────

    /**
     * Stat pills section — the container holding Total, Online, Offline, and Maintenance
     * count pills above the filter buttons in the Geo Location view.
     *
     * DOM observation: pills are rendered inside a flex row div above the filter bar.
     * Each pill is a small card containing a numeric value and a text label.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StatPills {

        /** 'Total' stat pill — shows count of all 12 devices */
        public static final String TOTAL =
                LocateBy.withCssText("div", "Total");

        /** 'Online' stat pill — shows count of online devices (6) */
        public static final String ONLINE =
                LocateBy.withCssText("div", "Online");

        /** 'Offline' stat pill — shows count of offline devices (3) */
        public static final String OFFLINE =
                LocateBy.withCssText("div", "Offline");

        /** 'Maintenance' stat pill — shows count of maintenance devices (3) */
        public static final String MAINTENANCE =
                LocateBy.withCssText("div", "Maintenance");

        /**
         * Numeric value "12" shown in the Total stat pill.
         * Uses :has() scoping to distinguish the geo-location pill from the summary bar.
         * Scoped inside the map view wrapper (not the global summary bar at top of page).
         */
        public static final String TOTAL_VALUE =
                LocateBy.css("div.maplibregl-map ~ div span:text-is('12'), " +
                        "[aria-label='Map'] ~ div span:text-is('12')");
    }

    // ──────────────────────────── Filter Buttons ────────────────────────────────────────

    /**
     * Filter button controls for the Geo Location map view.
     *
     * DOM observation: four button elements — "All", "Online(N)", "Offline(N)", "Maintenance(N)".
     * The active button carries bg-[#2563eb] text-white styling (Tailwind arbitrary value class).
     * withCssText uses substring matching so "Online(6)" matches the "Online" pattern.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Filters {

        /** "All" filter button — default active state on tab load */
        public static final String ALL =
                LocateBy.withExactCssText("button", "All");

        /** "Online(N)" filter button — N = count of online devices */
        public static final String ONLINE =
                LocateBy.withCssText("button", "Online(");

        /** "Offline(N)" filter button — N = count of offline devices */
        public static final String OFFLINE =
                LocateBy.withCssText("button", "Offline(");

        /** "Maintenance(N)" filter button — N = count of maintenance devices */
        public static final String MAINTENANCE =
                LocateBy.withCssText("button", "Maintenance(");

        /**
         * Currently active filter button — identified by its active CSS class.
         * The active state applies a blue Tailwind arbitrary class.
         * NOTE: [class*='bg-\\['] targets any button with a bg-[...] arbitrary value class.
         */
        public static final String ACTIVE_FILTER =
                LocateBy.css("button[class*='bg-\\[#2563eb\\]']");
    }

    // ──────────────────────────── Device Detail Card ─────────────────────────────────────

    /**
     * Device detail card that appears when a map marker pin is clicked.
     *
     * DOM reality (verified via live browser inspection — PS-29 healer run 2026-03-23):
     *  - The card is a div with classes: "bg-card border border-border rounded-lg p-5 relative"
     *  - The close button has NO aria-label — it is an absolutely positioned button with a
     *    Lucide X icon: class="absolute top-3 right-3 p-1 hover:bg-muted rounded transition-colors"
     *  - Using div.bg-card:has(button[aria-label='Close']) was WRONG — returns 0 results
     *  - Stable root selector: .bg-card.border.border-border.rounded-lg.p-5.relative
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DetailCard {

        /**
         * Outer card container — present when a pin has been clicked.
         * Uses the full class chain for specificity: bg-card + border + rounded-lg + p-5 + relative.
         */
        public static final String CONTAINER =
                LocateBy.css(".bg-card.border.border-border.rounded-lg.p-5.relative");

        /** Device name heading (h4) inside the detail card */
        public static final String DEVICE_NAME =
                LocateBy.css(".bg-card.border.border-border.rounded-lg.p-5.relative h4");

        /** Status badge inside the detail card (shows "Online" / "Offline" / "Maintenance") */
        public static final String STATUS_BADGE =
                LocateBy.css(".bg-card.border.border-border.rounded-lg.p-5.relative span[class*='rounded']");

        /**
         * Close (X) button — dismisses the detail card.
         * DOM reality: button with NO aria-label, positioned absolute top-right,
         * containing the Lucide lucide-x SVG icon.
         */
        public static final String CLOSE_BUTTON =
                LocateBy.css(".bg-card.border.border-border.rounded-lg.p-5.relative button.absolute.top-3.right-3");

        /** 'Serial:' field label row inside the detail card */
        public static final String SERIAL_FIELD =
                LocateBy.withCssText(".bg-card.border.border-border.rounded-lg.p-5.relative span", "Serial");

        /** 'Model:' field label row inside the detail card */
        public static final String MODEL_FIELD =
                LocateBy.withCssText(".bg-card.border.border-border.rounded-lg.p-5.relative span", "Model");

        /** 'Firmware:' field label row inside the detail card */
        public static final String FIRMWARE_FIELD =
                LocateBy.withCssText(".bg-card.border.border-border.rounded-lg.p-5.relative span", "Firmware");

        /** 'Health:' field label row inside the detail card */
        public static final String HEALTH_FIELD =
                LocateBy.withCssText(".bg-card.border.border-border.rounded-lg.p-5.relative span", "Health");

        /** 'Last Seen:' label — value may be em-dash (U+2014) when null */
        public static final String LAST_SEEN_FIELD =
                LocateBy.withCssText(".bg-card.border.border-border.rounded-lg.p-5.relative span", "Last Seen");
    }

    // ──────────────────────────── Map Controls ────────────────────────────────────────────

    /**
     * MapLibre built-in control buttons rendered in the top-right corner of the map.
     *
     * DOM observation: MapLibre renders standard control buttons with ARIA labels.
     * These are stable across MapLibre versions as they follow the MapLibre spec.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Controls {

        /** Zoom in (+) button */
        public static final String ZOOM_IN =
                LocateBy.css("button[aria-label='Zoom in']");

        /** Zoom out (-) button */
        public static final String ZOOM_OUT =
                LocateBy.css("button[aria-label='Zoom out']");

        /**
         * Compass / north-reset button.
         * ARIA label: "Drag to rotate map, click to reset north"
         * Using partial match for resilience across MapLibre patch versions.
         */
        public static final String COMPASS =
                LocateBy.css("button[aria-label*='reset north']");

        /**
         * Attribution panel toggle button (bottom-right of map).
         * Toggles the MapLibre + OpenStreetMap attribution text visibility.
         */
        public static final String ATTRIBUTION_TOGGLE =
                LocateBy.css("button[aria-label='Toggle attribution']");

        /**
         * MapLibre attribution container — bottom-right attribution panel.
         * Contains links to MapLibre and OpenStreetMap.
         */
        public static final String ATTRIBUTION_CONTAINER =
                LocateBy.css(".maplibregl-ctrl-attrib-inner");

        /** MapLibre attribution link */
        public static final String MAPLIBRE_LINK =
                LocateBy.withCssText(".maplibregl-ctrl-attrib-inner a", "MapLibre");

        /** OpenStreetMap attribution link */
        public static final String OSM_LINK =
                LocateBy.withCssText(".maplibregl-ctrl-attrib-inner a", "OpenStreetMap");
    }

    // ──────────────────────────── Map Legend ─────────────────────────────────────────────

    /**
     * Color legend panel rendered within the Geo Location view.
     * Shows the three device status colors: Online (green), Offline (red), Maintenance (orange).
     *
     * DOM observation: a small panel with three entries, each containing a colored indicator
     * and a status label. Located in the lower-right area of the map view.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Legend {

        /** Legend entry for Online status — green indicator */
        public static final String ONLINE_ENTRY =
                LocateBy.withCssText("div", "Online");

        /** Legend entry for Offline status — red indicator */
        public static final String OFFLINE_ENTRY =
                LocateBy.withCssText("div", "Offline");

        /** Legend entry for Maintenance status — orange indicator */
        public static final String MAINTENANCE_ENTRY =
                LocateBy.withCssText("div", "Maintenance");
    }
}
