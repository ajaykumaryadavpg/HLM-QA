package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the HLM Platform dashboard (home page after login).
 *
 * Locators verified via live browser DOM inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/
 *
 * Key findings (Tailwind CSS – no data-identifier attributes in the DOM):
 *  - KPI cards are div.bg-card containers; the full value class string is
 *    "text-3xl font-semibold text-foreground mb-1" — use [class*="text-3xl"] to match
 *  - Loading placeholder is U+2014 em-dash "—" shown in the text-3xl element before data arrives
 *  - Error banner: div.bg-red-50 rendered as a direct child of the main div.p-6.space-y-6 wrapper
 *  - Alerts panel container: div.bg-card containing h3 with exact text "Recent Alerts"
 *  - Alerts loading text: "Loading alerts…" (Unicode ellipsis U+2026, not three ASCII dots)
 *  - Alerts items: rendered as div elements inside div.space-y-3 — there is NO ul/li structure
 *  - Alerts error: when the alerts API fails the panel shows "No recent activity" in
 *    div.text-sm.text-muted-foreground — there is NO div.bg-red-50 inside the alerts card
 *  - Quick Action badges: span with 'absolute' positioning class inside main Quick Action cards (badge > 0 only — not rendered when count is 0)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardPage {

    /** Main page banner heading – "Dashboard" (h1) */
    public static final String DASHBOARD_HEADER  = LocateBy.css("h1");

    /**
     * Welcome paragraph inside main area – "Welcome back, {email} — here's your hardware lifecycle overview".
     * DOM change (commit 9819983 "UI Changes"): moved from h2 to p with class "text-base text-foreground/70".
     * The email is rendered inside a child span; the p's total textContent includes it.
     */
    public static final String WELCOME_MESSAGE   = LocateBy.withCssText("p", "Welcome back");

    /** Logged-in user email displayed in the sidebar */
    public static final String LOGGED_IN_USER    = LocateBy.css("aside span + div > span:last-child");

    /** Sign-out button in the sidebar */
    public static final String LOGOUT_BUTTON     = LocateBy.withExactCssText("button", "Sign out");

    /**
     * Refresh Dashboard button (circular arrow icon, top-right of the welcome row).
     * Addressed via its aria-label — stable across icon library updates (Story PS-3 / AC-14).
     */
    public static final String REFRESH_DASHBOARD_BUTTON = LocateBy.css("button[aria-label='Refresh dashboard']");

    /**
     * SVG icon element inside the Refresh Dashboard button.
     * Used to verify the {@code animate-spin} Tailwind class is applied during an active
     * refresh cycle (Story PS-6 / TC-PS6-REFRESH-03).
     * DOM: {@code button[aria-label='Refresh dashboard'] > svg}
     */
    public static final String REFRESH_BUTTON_ICON = LocateBy.css("button[aria-label='Refresh dashboard'] svg");

    // ─────────────────────────── KPI Cards ───────────────────────────

    /**
     * Locators for the KPI summary cards rendered at the top of the Dashboard
     * main area (Story PS-3).
     *
     * Each card is a div.bg-card containing:
     *  - div.text-3xl ... – the numeric count (or "—" while loading); full class: "text-3xl font-semibold text-foreground mb-0.5"
     *  - div.text-xs     – the card label (e.g. "Total Devices"); full class: "text-xs text-muted-foreground uppercase tracking-wide"
     * DOM change: label class changed from "text-sm text-muted-foreground" to "text-xs text-muted-foreground uppercase tracking-wide"
     * Quick Action card badges use absolute-positioned spans (class*='absolute') — only rendered when count > 0.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class KpiCard {

        /**
         * Any KPI value element still showing the loading placeholder "—" (U+2014 em-dash).
         *
         * DOM reality: the element class is "text-3xl font-semibold text-foreground mb-1"
         * — text-3xl is NOT a standalone CSS class, the div carries multiple utility classes.
         * Selector uses [class*="text-3xl"] attribute contains to match regardless of the
         * full class string.  :text-is() provides an exact match on U+2014 so it never
         * matches real numeric values.
         *
         * Verified via Playwright evaluate: codepoint 0x2014 confirmed as the placeholder char.
         * NOTE: This placeholder is only visible for ~1.5 s after page load — before data or
         * error state resolves.  For the settled error state (all APIs failed) use ZERO_FALLBACK.
         */
        public static final String LOADING_PLACEHOLDER =
                LocateBy.withExactCssText("div[class*='text-3xl']", "\u2014");

        /**
         * Any KPI value element showing the zero fallback "0" after an API error.
         *
         * DOM reality: when GraphQL APIs are aborted or return errors the app transitions
         * from the "—" loading placeholder to "0" (the error/fallback value) within ~1.5 s.
         * The element class is identical to LOADING_PLACEHOLDER:
         *   "text-3xl font-semibold text-foreground mb-1"
         *
         * Use this locator (with a pre-assertion wait via byWaitingFor) when verifying the
         * settled error state — i.e. after all APIs have failed and the app has resolved.
         */
        public static final String ZERO_FALLBACK =
                LocateBy.withExactCssText("div[class*='text-3xl']", "0");

        // ── Total Devices ──────────────────────────────────────────────
        /**
         * Numeric count of all devices (up to 100).
         * NOTE: [class*="text-3xl"] is required because the value div carries multiple
         * Tailwind utility classes; a bare div.text-3xl class selector fails.
         * DOM change: label element class changed from "text-sm text-muted-foreground" to
         * "text-xs text-muted-foreground uppercase tracking-wide" — use [class*='text-xs'] to match.
         */
        public static final String TOTAL_DEVICES_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')) div[class*='text-3xl']");

        /**
         * Offline-device badge count shown on the Inventory quick-action card (main content area).
         * DOM change (commit 55a2a04): badge is now conditionally rendered (only when count > 0)
         * and uses absolute-positioned span — class no longer includes 'bg-orange-500'.
         */
        public static final String OFFLINE_DEVICES_BADGE =
                LocateBy.css("main a[href='/inventory'] span[class*='absolute']");

        // ── Active Deployments ─────────────────────────────────────────
        /** Count of in-progress service orders */
        public static final String ACTIVE_DEPLOYMENTS_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Active Deployments')) div[class*='text-3xl']");

        /**
         * Scheduled-orders badge count shown on the Account Service quick-action card (main content area).
         * DOM change (commit 55a2a04): badge conditionally rendered (count > 0 only); uses absolute span.
         */
        public static final String SCHEDULED_ORDERS_BADGE =
                LocateBy.css("main a[href='/account-service'] span[class*='absolute']");

        // ── Pending Approvals ──────────────────────────────────────────
        /** Combined count of pending firmware + pending compliance records */
        public static final String PENDING_APPROVALS_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Pending Approvals')) div[class*='text-3xl']");

        // ── Health Score ───────────────────────────────────────────────
        /**
         * Health Score KPI card value (4th KPI card).
         * NOTE: The DOM label is "Health Score"; "Compliance Score" does not exist in the UI.
         */
        public static final String HEALTH_SCORE_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Health Score')) div[class*='text-3xl']");

        // ── Icon locators (color-class verification, Story PS-4 / AC-2,3,4,5) ──────

        /**
         * Blue Package icon inside the "Total Devices" KPI card.
         * Targets an SVG element carrying a blue Tailwind color utility class.
         */
        public static final String TOTAL_DEVICES_ICON =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')) svg[class*='text-blue']");

        /**
         * Green Download icon inside the "Active Deployments" KPI card.
         * Targets an SVG element carrying a green Tailwind color utility class.
         */
        public static final String ACTIVE_DEPLOYMENTS_ICON =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Active Deployments')) svg[class*='text-green']");

        /**
         * Orange Shield icon inside the "Pending Approvals" KPI card.
         * Targets an SVG element carrying an orange Tailwind color utility class.
         */
        public static final String PENDING_APPROVALS_ICON =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Pending Approvals')) svg[class*='text-orange']");

        /**
         * Teal Check-circle icon inside the "Health Score" KPI card.
         * DOM change: icon class is "text-teal-600 dark:text-teal-400" — NOT text-green.
         * The icon is a lucide-circle-check-big SVG carrying a teal Tailwind color class.
         * Using [class*='text-teal'] to match the actual icon color.
         */
        public static final String HEALTH_SCORE_ICON =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Health Score')) svg[class*='text-teal']");

        /**
         * The entire KPI card grid row container — used for responsive layout assertions.
         * Actual DOM class: "grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4"
         * The grid switches from grid-cols-1 (mobile) → grid-cols-2 (tablet) → grid-cols-4 (desktop).
         * Parent of this grid is div.p-6.space-y-6 (the main content wrapper).
         */
        public static final String KPI_GRID_CONTAINER =
                LocateBy.css("div[class*='grid-cols-4']:has(div.bg-card:has(div[class*='text-xs']:text-is('Total Devices')))");

        /**
         * Label text locators — used for dark-mode visibility assertions (Story PS-4 / AC-11).
         * DOM change: actual label class is "text-xs text-muted-foreground uppercase tracking-wide"
         * — using [class*="text-xs"] instead of [class*="text-sm"] to match the live DOM.
         */
        public static final String TOTAL_DEVICES_LABEL =
                LocateBy.withCssText("div.bg-card div[class*='text-xs']", "Total Devices");

        public static final String ACTIVE_DEPLOYMENTS_LABEL =
                LocateBy.withCssText("div.bg-card div[class*='text-xs']", "Active Deployments");

        public static final String PENDING_APPROVALS_LABEL =
                LocateBy.withCssText("div.bg-card div[class*='text-xs']", "Pending Approvals");

        public static final String HEALTH_SCORE_LABEL =
                LocateBy.withCssText("div.bg-card div[class*='text-xs']", "Health Score");
    }

    // ──────────────────────────── Error Banner ────────────────────────────

    /**
     * Red error banner displayed when any dashboard API call fails (Story PS-3).
     *
     * DOM reality (verified via live inspection):
     *  - The banner element: div.bg-red-50 with full class
     *    "bg-red-50 dark:bg-red-900/20 border border-red-200 dark:border-red-800
     *     rounded-lg p-4 text-red-700 dark:text-red-400"
     *  - It is rendered as a DIRECT child of the main content wrapper (div.p-6.space-y-6),
     *    appearing between the welcome row and the KPI grid when at least one KPI API fails.
     *  - The text content is "Unable to load dashboard data. Please refresh the page."
     *    (hardcoded user-friendly message since commit 55a2a04 "UI Fixes" — no longer the raw API error string)
     *  - NOTE: The alerts panel does NOT render a div.bg-red-50 when its own API fails;
     *    instead it falls back to "No recent activity" text inside div.space-y-3.
     *  - The selector "div.p-6.space-y-6 > div.bg-red-50" is CORRECT as written.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorBanner {

        /** Outer container – visible when any KPI API returns an error */
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
     *
     * DOM reality (verified via live inspection and API interception):
     *  - Container class: "bg-card border border-border rounded-lg p-6"
     *  - h3 class: "text-lg text-foreground" with text "Recent Alerts"
     *  - Items wrapper: div.space-y-3 (NOT ul — there are NO ul or li elements)
     *  - Individual items are div elements inside div.space-y-3
     *  - Loading text: "Loading alerts…" uses Unicode ellipsis U+2026 (NOT three ASCII dots "...")
     *  - When the alerts API fails: shows "No recent activity" in div.text-sm.text-muted-foreground
     *    inside div.space-y-3 — there is NO div.bg-red-50 within the alerts card
     *  - Empty state text: "No recent activity"
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AlertsPanel {

        /** Outer panel container – div.bg-card that holds the "Recent Alerts" h3 */
        public static final String CONTAINER =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts'))");

        /**
         * Loading text shown while audit-log data is being fetched.
         * IMPORTANT: The app renders "Loading alerts…" with Unicode ellipsis U+2026,
         * NOT three ASCII dots.  The :has-text() filter matches by substring so both
         * "Loading alerts" (no ellipsis) and the full string will match.
         */
        public static final String LOADING_INDICATOR =
                LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "Loading alerts");

        /**
         * Error message shown when the audit-log API call fails.
         * The alerts panel renders a distinct div.bg-red-50 (red box) inside the card,
         * BEFORE the div.space-y-3 items wrapper.
         * Text (since commit 55a2a04 "UI Fixes"): "Unable to load recent alerts. Please try again."
         * (Authorization failures show "You don't have permission to view recent alerts.")
         * This is separate from the global KPI error banner.
         *
         * DOM (verified via live GraphQL interception):
         *   div.bg-card:has(h3:text-is('Recent Alerts'))
         *     > div.bg-red-50.dark:bg-red-900/20.border... (alert-specific error)
         *     > div.space-y-3 (items / empty-state)
         */
        public static final String ERROR_MESSAGE =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.bg-red-50");

        /** Empty-state message shown when no audit logs exist in the last 24 h */
        public static final String EMPTY_STATE =
                LocateBy.withCssText("div.bg-card:has(h3:text-is('Recent Alerts'))", "No recent activity");

        /**
         * "View all" link inside the Recent Alerts panel that navigates to /analytics.
         * DOM: anchor element with href="/analytics" inside the alerts card header row.
         */
        public static final String VIEW_ALL_LINK =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) a[href='/analytics']");

        /**
         * Orange warning SVG icon inside a failed-action alert item row.
         * Rendered when auditStatus='failed' or action text contains 'failed'.
         * DOM: svg element with a Tailwind orange color class (text-orange-*) inside div.bg-muted.
         */
        public static final String FAILED_ACTION_ICON =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted svg[class*='text-orange']");

        /**
         * Green check SVG icon inside an approved-action alert item row.
         * Rendered when auditStatus='approved' or action text contains 'approved'/'approve'.
         * DOM: svg element with a Tailwind green color class (text-green-*) inside div.bg-muted.
         */
        public static final String APPROVED_ACTION_ICON =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted svg[class*='text-green']");

        /**
         * Blue info SVG icon inside a generic (non-failed, non-approved) alert item row.
         * DOM: svg element with a Tailwind blue color class (text-blue-*) inside div.bg-muted.
         */
        public static final String INFO_ACTION_ICON =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted svg[class*='text-blue']");

        /**
         * Repeating alert items – one per audit-log entry.
         * IMPORTANT: Items are NOT rendered as ul/li — the DOM uses div.bg-muted divs
         * inside div.space-y-3.  The empty-state ("No recent activity") is also a div
         * inside space-y-3 but carries class "text-sm text-muted-foreground" (no bg-muted).
         * Scoping to div.bg-muted ensures only actual item rows match, not the empty-state.
         *
         * Item DOM: div.flex.items-start.gap-3.p-3.bg-muted.rounded-lg
         * Empty-state DOM: div.text-sm.text-muted-foreground (no bg-muted)
         *
         * Old (WRONG): ul li  — no ul/li elements exist
         * Also WRONG: div.space-y-3 > div  — matches empty-state div too
         * Correct: div.space-y-3 > div.bg-muted  — only actual item rows
         */
        public static final String ALERT_ITEM =
                LocateBy.css("div.bg-card:has(h3:text-is('Recent Alerts')) div.space-y-3 > div.bg-muted");
    }

    // ──────────────────────────── Quick Actions ──────────────────────────

    /**
     * Locators for the Quick Actions section rendered on the Dashboard (Story PS-6).
     * Each card is a clickable link inside the quick-actions grid.
     *
     * Badge locators are scoped to {@code main} to exclude the sidebar nav links
     * (which share the same href but do not carry badge spans).
     *
     * Card locators with the {@code _MAIN} suffix are scoped to {@code main} and should
     * be used when counting or asserting Quick Action cards — prevents false positives
     * from matching the sidebar navigation links with the same href.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class QuickActions {

        /**
         * Quick Actions grid container in the main content area.
         * Used for counting Quick Action cards (expects exactly 4 descendant cards).
         */
        public static final String QUICK_ACTIONS_GRID =
                LocateBy.css("main div[class*='grid']:has(a[href='/inventory'])");

        // ── View Inventory ─────────────────────────────────────────────

        /**
         * "View Inventory" quick-action card — not scoped (matches sidebar + main).
         * Prefer {@link #VIEW_INVENTORY_CARD_MAIN} for assertions inside main content.
         */
        public static final String VIEW_INVENTORY_CARD =
                LocateBy.css("a[href='/inventory']");

        /** "View Inventory" quick-action card — scoped to {@code main}, excludes sidebar nav link */
        public static final String VIEW_INVENTORY_CARD_MAIN =
                LocateBy.css("main a[href='/inventory']");

        /** "View Inventory" label text inside the card */
        public static final String VIEW_INVENTORY_LABEL =
                LocateBy.withCssText("main a[href='/inventory']", "View Inventory");

        /** SVG icon inside the "View Inventory" card */
        public static final String VIEW_INVENTORY_ICON =
                LocateBy.css("main a[href='/inventory'] svg");

        /**
         * Offline-device badge on the "View Inventory" quick-action card.
         * DOM change (commit 55a2a04): badge is conditionally rendered (only when count > 0);
         * the span class is now absolute-positioned — 'bg-orange-500' was replaced with bg-[#1e293b].
         */
        public static final String VIEW_INVENTORY_BADGE =
                LocateBy.css("main a[href='/inventory'] span[class*='absolute']");

        // ── Schedule Service ───────────────────────────────────────────

        /**
         * "Schedule Service" quick-action card — not scoped.
         * Prefer {@link #SCHEDULE_SERVICE_CARD_MAIN} for assertions inside main content.
         */
        public static final String SCHEDULED_SERVICE_CARD =
                LocateBy.css("a[href='/account-service']");

        /** "Schedule Service" quick-action card — scoped to {@code main} */
        public static final String SCHEDULE_SERVICE_CARD_MAIN =
                LocateBy.css("main a[href='/account-service']");

        /** "Schedule Service" label text inside the card */
        public static final String SCHEDULE_SERVICE_LABEL =
                LocateBy.withCssText("main a[href='/account-service']", "Schedule Service");

        /** SVG icon inside the "Schedule Service" card */
        public static final String SCHEDULE_SERVICE_ICON =
                LocateBy.css("main a[href='/account-service'] svg");

        /** Scheduled-orders badge on the "Schedule Service" quick-action card */
        public static final String SCHEDULE_SERVICE_BADGE =
                LocateBy.css("main a[href='/account-service'] span[class*='absolute']");

        // ── Deploy Firmware ────────────────────────────────────────────

        /**
         * "Deploy Firmware" quick-action card.
         * Uses {@code .relative.bg-card} to disambiguate from the sidebar nav link
         * that also has {@code href='/deployment'} but lacks these classes.
         */
        public static final String DEPLOY_FIRMWARE_CARD =
                LocateBy.css("a[href='/deployment'].relative.bg-card");

        /** "Deploy Firmware" label text inside the card */
        public static final String DEPLOY_FIRMWARE_LABEL =
                LocateBy.withCssText("a[href='/deployment'].relative.bg-card", "Deploy Firmware");

        /** SVG icon inside the "Deploy Firmware" card */
        public static final String DEPLOY_FIRMWARE_ICON =
                LocateBy.css("a[href='/deployment'].relative.bg-card svg");

        /** Pending-firmware badge on the "Deploy Firmware" quick-action card */
        public static final String DEPLOY_FIRMWARE_BADGE =
                LocateBy.css("a[href='/deployment'].relative.bg-card span[class*='absolute']");

        // ── Check Compliance ───────────────────────────────────────────

        /**
         * "Check Compliance" quick-action card — not scoped.
         * Prefer {@link #CHECK_COMPLIANCE_CARD_MAIN} for assertions inside main content.
         */
        public static final String COMPLIANCE_LINK =
                LocateBy.css("a[href='/compliance']");

        /** "Check Compliance" quick-action card — scoped to {@code main} */
        public static final String CHECK_COMPLIANCE_CARD_MAIN =
                LocateBy.css("main a[href='/compliance']");

        /** "Check Compliance" label text inside the card */
        public static final String CHECK_COMPLIANCE_LABEL =
                LocateBy.withCssText("main a[href='/compliance']", "Check Compliance");

        /** SVG icon inside the "Check Compliance" card */
        public static final String CHECK_COMPLIANCE_ICON =
                LocateBy.css("main a[href='/compliance'] svg");

        /** Pending-compliance badge on the "Check Compliance" quick-action card */
        public static final String CHECK_COMPLIANCE_BADGE =
                LocateBy.css("main a[href='/compliance'] span[class*='absolute']");
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

        // ── Per-service Operational/Degraded locators ──────────────────────

        /** Operational (green) label scoped to the Deployment Service row */
        public static final String DEPLOYMENT_SERVICE_OPERATIONAL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Deployment Service') span.text-green-600");

        /** Degraded (orange) label scoped to the Deployment Service row */
        public static final String DEPLOYMENT_SERVICE_DEGRADED =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Deployment Service') span.text-orange-600");

        /** Operational (green) label scoped to the Compliance Engine row */
        public static final String COMPLIANCE_ENGINE_OPERATIONAL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Compliance Engine') span.text-green-600");

        /** Degraded (orange) label scoped to the Compliance Engine row */
        public static final String COMPLIANCE_ENGINE_DEGRADED =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Compliance Engine') span.text-orange-600");

        /** Operational (green) label scoped to the Asset Database row */
        public static final String ASSET_DATABASE_OPERATIONAL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Asset Database') span.text-green-600");

        /** Degraded (orange) label scoped to the Asset Database row */
        public static final String ASSET_DATABASE_DEGRADED =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Asset Database') span.text-orange-600");

        /** Operational (green) label scoped to the Analytics Platform row — always Operational per spec */
        public static final String ANALYTICS_PLATFORM_OPERATIONAL =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Analytics Platform') span.text-green-600");

        /** Degraded (orange) label scoped to the Analytics Platform row — must NOT be present per spec */
        public static final String ANALYTICS_PLATFORM_DEGRADED =
                LocateBy.css("div.bg-card:has(h3:text-is('System Status')) div.space-y-4 > div:has-text('Analytics Platform') span.text-orange-600");
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
