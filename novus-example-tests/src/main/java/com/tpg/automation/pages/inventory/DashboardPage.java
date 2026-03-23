package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the HLM Platform dashboard (home page after login).
 *
 * Locators verified via browser inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/
 *
 * Key findings (Tailwind CSS – no data-identifier attributes in the DOM):
 *  - KPI cards are div.bg-card containers; values use class "text-3xl font-semibold text-foreground mb-1"
 *  - Loading placeholder "—" appears in the text-3xl element before data arrives
 *  - Error banner: div.bg-red-50 as a direct child of div.p-6.space-y-6
 *  - Alerts panel: div.bg-card containing h3 with exact text "Recent Alerts"
 *  - Navigation badges: span.bg-orange-500 inside sidebar nav links
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardPage {

    /** Main page banner heading – "Dashboard" (h1) */
    public static final String DASHBOARD_HEADER  = LocateBy.withExactCssText("h1", "Dashboard");

    /** Welcome heading inside main area – "Welcome back, …" */
    public static final String WELCOME_MESSAGE   = LocateBy.withCssText("h2", "Welcome back");

    /** Logged-in user email displayed in the sidebar */
    public static final String LOGGED_IN_USER    = LocateBy.css("aside span + div > span:last-child");

    /** Sign-out button in the sidebar */
    public static final String LOGOUT_BUTTON     = LocateBy.withExactCssText("button", "Sign out");

    /**
     * Refresh Dashboard button (circular arrow icon, top-right of the welcome row).
     * Addressed via its aria-label — stable across icon library updates (Story PS-3 / AC-14).
     */
    public static final String REFRESH_DASHBOARD_BUTTON = LocateBy.css("button[aria-label='Refresh dashboard']");

    // ─────────────────────────── KPI Cards ───────────────────────────

    /**
     * Locators for the KPI summary cards rendered at the top of the Dashboard
     * main area (Story PS-3).
     *
     * Each card is a div.bg-card containing:
     *  - div.text-3xl ... – the numeric count (or "—" while loading)
     *  - div.text-sm     – the card label (e.g. "Total Devices")
     * Navigation sidebar badges (span.bg-orange-500) show offline / scheduled counts.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class KpiCard {

        /**
         * Any kpi value element still showing the loading placeholder "—".
         * Uses :text-is() for exact case-sensitive match so it never matches
         * real numeric values that happen to contain a dash.
         */
        public static final String LOADING_PLACEHOLDER =
                LocateBy.withExactCssText("div.text-3xl", "\u2014");

        // ── Total Devices ──────────────────────────────────────────────
        /** Numeric count of all devices (up to 100) */
        public static final String TOTAL_DEVICES_VALUE =
                LocateBy.css("div.bg-card:has(div.text-sm:text-is('Total Devices')) div.text-3xl");

        /** Offline-device badge count shown on the Inventory quick-action card (main content area) */
        public static final String OFFLINE_DEVICES_BADGE =
                LocateBy.css("main a[href='/inventory'] span.bg-orange-500");

        // ── Active Deployments ─────────────────────────────────────────
        /** Count of in-progress service orders */
        public static final String ACTIVE_DEPLOYMENTS_VALUE =
                LocateBy.css("div.bg-card:has(div.text-sm:text-is('Active Deployments')) div.text-3xl");

        /** Scheduled-orders badge count shown on the Account Service quick-action card (main content area) */
        public static final String SCHEDULED_ORDERS_BADGE =
                LocateBy.css("main a[href='/account-service'] span.bg-orange-500");

        // ── Pending Approvals ──────────────────────────────────────────
        /** Combined count of pending firmware + pending compliance records */
        public static final String PENDING_APPROVALS_VALUE =
                LocateBy.css("div.bg-card:has(div.text-sm:text-is('Pending Approvals')) div.text-3xl");

        // ── Health Score ───────────────────────────────────────────────
        /**
         * Health Score KPI card value (4th KPI card).
         * NOTE: The DOM label is "Health Score"; "Compliance Score" does not exist in the UI.
         */
        public static final String HEALTH_SCORE_VALUE =
                LocateBy.css("div.bg-card:has(div.text-sm:text-is('Health Score')) div.text-3xl");
    }

    // ──────────────────────────── Error Banner ────────────────────────────

    /**
     * Red error banner displayed when any dashboard API call fails (Story PS-3).
     * The banner is a div.bg-red-50 rendered as a direct child of the main
     * div.p-6.space-y-6 content area.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorBanner {

        /** Outer container – visible on any API error */
        public static final String CONTAINER =
                LocateBy.css("div.p-6.space-y-6 > div.bg-red-50");

        /** The banner element itself also carries the error message text */
        public static final String MESSAGE =
                LocateBy.css("div.p-6.space-y-6 > div.bg-red-50");
    }

    // ──────────────────────────── Alerts Panel ────────────────────────────

    /**
     * Recent-alerts panel rendered in the right column of the Dashboard.
     * Populated with audit-log entries from the last 24 hours (Story PS-3).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AlertsPanel {

        /** Outer panel container – div.bg-card that holds the "Recent Alerts" h3 */
        public static final String CONTAINER =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts'))");

        /** Loading text shown while audit-log data is being fetched */
        public static final String LOADING_INDICATOR =
                LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "Loading alerts");

        /** Error element shown when the audit-log API call fails */
        public static final String ERROR_MESSAGE =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.bg-red-50");

        /** Empty-state message shown when no audit logs exist in the last 24 h */
        public static final String EMPTY_STATE =
                LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "No recent activity");

        /** Repeating list items – one per audit-log entry */
        public static final String ALERT_ITEM =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) ul li");
    }

    // ──────────────────────────── Quick Actions ──────────────────────────

    /**
     * Locators for the Quick Actions section rendered on the Dashboard.
     * Each card is a clickable link inside the quick-actions grid.
     *
     * Badge locators are scoped to {@code main} to exclude the sidebar nav links
     * (which share the same href but do not carry badge spans).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QuickActions {

        /** "View Inventory" quick-action card */
        public static final String VIEW_INVENTORY_CARD =
                LocateBy.css("a[href='/inventory']");

        /** Offline-device badge on the "View Inventory" quick-action card */
        public static final String VIEW_INVENTORY_BADGE =
                LocateBy.css("main a[href='/inventory'] span.bg-orange-500");

        /** "Schedule Service" quick-action card */
        public static final String SCHEDULED_SERVICE_CARD =
                LocateBy.css("a[href='/account-service']");

        /** Scheduled-orders badge on the "Schedule Service" quick-action card */
        public static final String SCHEDULE_SERVICE_BADGE =
                LocateBy.css("main a[href='/account-service'] span.bg-orange-500");

        /** "Deploy Firmware" quick-action card */
        public static final String DEPLOY_FIRMWARE_CARD =
                LocateBy.css("a[href='/deployment'].relative.bg-card");

        /** Pending-firmware badge on the "Deploy Firmware" quick-action card */
        public static final String DEPLOY_FIRMWARE_BADGE =
                LocateBy.css("a[href='/deployment'].relative.bg-card span.bg-orange-500");

        /** "Check Compliance" quick-action card */
        public static final String COMPLIANCE_LINK =
                LocateBy.css("a[href='/compliance']");

        /** Pending-compliance badge on the "Check Compliance" quick-action card */
        public static final String CHECK_COMPLIANCE_BADGE =
                LocateBy.css("main a[href='/compliance'] span.bg-orange-500");
    }

    // ──────────────────────────── System Status ──────────────────────────

    /**
     * Locators for the System Status panel rendered in the bottom-right of the Dashboard.
     * Displays health percentages for 4 services derived from live KPI data (Story PS-3 / AC-18).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class SystemStatus {

        /** Outer panel container */
        public static final String CONTAINER =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status'))");

        /** Individual service rows — 4 expected (Deployment Service, Compliance Engine, Asset Database, Analytics Platform) */
        public static final String SERVICE_ITEM =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div");

        /** Green "Operational" status label (health ≥ 90%) */
        public static final String OPERATIONAL_LABEL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) span.text-green-600");

        /** Orange "Degraded" status label (health < 90%) */
        public static final String DEGRADED_LABEL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) span.text-orange-600");
    }

    // ──────────────────────────── Navigation ─────────────────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class NavMenu {
        /** Sidebar nav link → /inventory  (text: "Inventory & Assets") */
        public static final String INVENTORY_LINK = LocateBy.css("nav a[href='/inventory']");

        /** Sidebar nav link → /analytics  (text: "Reporting & Analytics") */
        public static final String ANALYTICS_LINK = LocateBy.css("nav a[href='/analytics']");

        /** Sidebar nav link → /compliance (text: "Firmware Compliance") */
        public static final String COMPLIANCE_LINK = LocateBy.css("nav a[href='/compliance']");

        /** Sidebar nav link → /deployment */
        public static final String DEPLOYMENT_LINK = LocateBy.css("nav a[href='/deployment']");

        /** Sidebar nav link → /reports */
        public static final String REPORTS_LINK = LocateBy.css("nav a[href='/reports']");
    }
}
