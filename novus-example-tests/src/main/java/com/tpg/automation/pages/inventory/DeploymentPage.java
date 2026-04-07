package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the HLM Platform Deployment page (/deployment).
 *
 * Locators verified via live browser DOM inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/deployment
 *
 * Key DOM findings:
 *  - Two ARIA tabs: "Firmware Versions" (id=tab-firmware) and "Audit Log" (id=tab-audit)
 *  - Firmware tab panel: role="tabpanel" aria-labelledby="tab-firmware"
 *  - Firmware cards: .bg-card inside the tabpanel; each has a status badge span
 *  - Status badge: span.inline-flex.rounded-full with status-specific bg color class
 *    Current (pre-PS-34): Pending=bg-orange-100, Approved=bg-green-100, Deprecated=bg-red-100
 *    Post-PS-34 target:   Screening, Staged, Active, Deprecated, Recalled
 *  - Upload Firmware modal: [role="dialog"]; form fields use IDs fw-name, fw-version, etc.
 *  - Submit button is HTML-disabled until all required fields (Name, Version, Device Model,
 *    Firmware File) are populated — enforced via JS state, not HTML5 required attr on file input
 *  - Release Date is a popover-trigger button, not a standard <input>
 *  - Audit log entries: div.space-y-3 > .bg-card inside the audit tab content area
 *
 * @jira PS-34 (Story: FirmwareFamily DynamoDB Entity & AppSync Schema)
 * @jira PS-41 (QA Sub-task)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeploymentPage {

    // ──────────────────────────── Tab Navigation ──────────────────────────────

    /** "Firmware Versions" tab button — ARIA id="tab-firmware" */
    public static final String FIRMWARE_VERSIONS_TAB =
            LocateBy.css("button#tab-firmware");

    /** "Audit Log" tab button — ARIA id="tab-audit" */
    public static final String AUDIT_LOG_TAB =
            LocateBy.css("button#tab-audit");

    /** Currently active/selected tab — has aria-selected="true" */
    public static final String ACTIVE_TAB =
            LocateBy.css("[role='tab'][aria-selected='true']");

    // ──────────────────────────── Search / Filter ────────────────────────────

    /**
     * Search text input shared by both tab panels.
     * Placeholder changes per tab: "Search name, version, model…" (firmware)
     * vs "Search action, resource, ID…" (audit log).
     */
    public static final String SEARCH_INPUT =
            LocateBy.css("input#tab-search");

    // ──────────────────────────── Page-level Actions ─────────────────────────

    /**
     * "Upload Firmware" primary action button at the top of the Deployment page.
     * DOM: btn-primary button containing an upload SVG icon and "Upload Firmware" text.
     */
    public static final String UPLOAD_FIRMWARE_BUTTON =
            LocateBy.withCssText("button", "Upload Firmware");

    // ──────────────────────────── Deployment KPI Cards ───────────────────────

    /**
     * KPI stat cards shown at the top of the Deployment page.
     * Four cards: Approved Versions (green), Pending Review (orange),
     * Total Downloads (blue), Recent Activities (purple).
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class StatsCard {

        /** Count of firmware items with status Approved (green left-border card) */
        public static final String APPROVED_VERSIONS_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Approved Versions')) div[class*='text-3xl']");

        /** Count of firmware items currently under review / pending approval (orange left-border card) */
        public static final String PENDING_REVIEW_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Pending Review')) div[class*='text-3xl']");

        /** Total firmware download count (blue left-border card) */
        public static final String TOTAL_DOWNLOADS_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Total Downloads')) div[class*='text-3xl']");

        /** Recent audit-log activity count (purple left-border card) */
        public static final String RECENT_ACTIVITIES_VALUE =
                LocateBy.css("div.bg-card:has(div[class*='text-xs']:text-is('Recent Activities')) div[class*='text-3xl']");
    }

    // ──────────────────────────── Firmware Cards ─────────────────────────────

    /**
     * Locators for the firmware version cards rendered inside the Firmware Versions tab panel.
     *
     * DOM reality:
     *  - Tabpanel: role="tabpanel" aria-labelledby="tab-firmware"
     *  - Cards grid: grid-cols-1 md:grid-cols-2 lg:grid-cols-3
     *  - Each card: div.bg-card.border.rounded-lg.p-6 inside the tabpanel
     *  - Status badge: span.inline-flex.items-center.gap-1.px-3.py-1.text-xs.rounded-full
     *    with status-specific background colour class appended
     *  - Details expand toggle: button[aria-expanded] — toggles an inline details section
     *  - Download button: button.btn-primary (only present for Approved / Active firmware)
     *  - Deprecate button: button.border-red-300 (only for Approved / Active firmware)
     *  - Approve button: text "Approve" (only for Pending firmware)
     *  - Terminal-state cards (Deprecated, Recalled): only show the Details toggle button
     *
     * Post-PS-34 note: status enum values change from
     *   {Pending, Approved, Deprecated, Rejected}  →  {Screening, Staged, Active, Deprecated, Recalled}
     * Locators prefixed _PS34_ are placeholders that will be activated after PS-34 ships.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FirmwareCard {

        /** Tab-panel container — role="tabpanel" aria-labelledby="tab-firmware" */
        public static final String PANEL =
                LocateBy.css("[aria-labelledby='tab-firmware']");

        /** Any firmware card inside the Firmware Versions tabpanel */
        public static final String CARD =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card");

        /** Firmware name heading (h3) inside a card */
        public static final String CARD_HEADING =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card h3.text-lg");

        // ── Status Badges — current pre-PS-34 values ────────────────────────

        /** Any status badge span inside a firmware card */
        public static final String STATUS_BADGE =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full");

        /**
         * "Pending" status badge — orange background (bg-orange-100).
         * Valid in current schema; maps to "Screening" or "Staged" after PS-34.
         */
        public static final String PENDING_BADGE =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card span.bg-orange-100");

        /**
         * "Approved" status badge — green background (bg-green-100).
         * Valid in current schema; replaced by "Active" after PS-34.
         */
        public static final String APPROVED_BADGE =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card span.bg-green-100");

        /**
         * "Deprecated" status badge — red background (bg-red-100).
         * Valid in both current and post-PS-34 schemas.
         */
        public static final String DEPRECATED_BADGE =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card span.bg-red-100");

        // ── Status Badges — post-PS-34 new enum values ──────────────────────
        // These locators are activated once PS-34 ships and the UI is updated.

        /** "Screening" badge — first stage in the PS-34 firmware lifecycle */
        public static final String _PS34_SCREENING_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Screening");

        /** "Staged" badge — second stage in the PS-34 lifecycle */
        public static final String _PS34_STAGED_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Staged");

        /** "Active" badge — production-ready; replaces "Approved" in PS-34 */
        public static final String _PS34_ACTIVE_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Active");

        /** "Recalled" badge — terminal state added in PS-34 */
        public static final String _PS34_RECALLED_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Recalled");

        // ── Legacy / removed badges — should be ABSENT post-PS-34 ───────────

        /** "Pending" text badge — must NOT appear after PS-34 ships */
        public static final String _PS34_ABSENT_PENDING_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Pending");

        /** "Approved" text badge — must NOT appear after PS-34 ships */
        public static final String _PS34_ABSENT_APPROVED_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Approved");

        /** "Rejected" text badge — must NOT appear after PS-34 ships */
        public static final String _PS34_ABSENT_REJECTED_BADGE =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card span.inline-flex.rounded-full", "Rejected");

        // ── Card Action Buttons ──────────────────────────────────────────────

        /**
         * Details expand/collapse toggle button — present on ALL firmware cards.
         * DOM: button[aria-expanded="false"|"true"] inside a .bg-card.
         * Clicking toggles an inline details section showing release notes, checksum, uploader.
         */
        public static final String DETAILS_TOGGLE =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card button[aria-expanded]");

        /**
         * Download primary action button — present only on Approved (pre-PS-34) / Active (post-PS-34) cards.
         * DOM: button.btn-primary inside a .bg-card.
         */
        public static final String DOWNLOAD_BUTTON =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card button.btn-primary");

        /**
         * Deprecate action button — red-bordered; present only on Approved / Active cards.
         * DOM: button.border-red-300 inside a .bg-card.
         */
        public static final String DEPRECATE_BUTTON =
                LocateBy.css("[aria-labelledby='tab-firmware'] .bg-card button.border-red-300");

        /**
         * Approve action button — present only on Pending firmware cards (pre-PS-34).
         * After PS-34 the equivalent lifecycle button may change label/behaviour.
         */
        public static final String APPROVE_BUTTON =
                LocateBy.withCssText("[aria-labelledby='tab-firmware'] .bg-card button", "Approve");
    }

    // ──────────────────────────── Upload Firmware Modal ──────────────────────

    /**
     * Locators for the "Upload Firmware" Radix UI dialog modal.
     *
     * DOM reality:
     *  - Container: div[role="dialog"] data-slot="dialog-content" — appears in-page, not a native dialog
     *  - Title: h2[data-slot="dialog-title"] containing "Upload Firmware"
     *  - All required text inputs use HTML id attributes: fw-name, fw-version, fw-device-model
     *  - Manufacturer (optional) uses id fw-manufacturer
     *  - File picker: input#fw-file type="file" (NOT required in HTML but JS disables Submit until chosen)
     *  - Release Date: button[data-slot="popover-trigger"] — calendar popover, NOT a standard input
     *  - Release Notes: textarea#fw-release-notes (optional)
     *  - Submit: button[type="submit"] in dialog-footer — HTML disabled until all required fields filled
     *  - Cancel: button[type="button"] in dialog-footer
     *  - Close X: button.absolute.top-5.right-5 in dialog (sr-only "Close" label)
     *
     * Post-PS-34 gap: a "Firmware Family" dropdown/select is expected (TC-34.7.08) but
     * is currently absent from the UI — FIRMWARE_FAMILY_LABEL will not be visible until
     * the Upload Firmware form is updated as part of the PS-34 front-end work.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class UploadModal {

        /** Modal dialog container element */
        public static final String CONTAINER =
                LocateBy.css("[role='dialog']");

        /** Modal title heading — "Upload Firmware" */
        public static final String TITLE =
                LocateBy.css("[data-slot='dialog-title']");

        /** Name required text field */
        public static final String NAME_FIELD =
                LocateBy.css("input#fw-name");

        /** Version required text field */
        public static final String VERSION_FIELD =
                LocateBy.css("input#fw-version");

        /** Device Model required text field */
        public static final String DEVICE_MODEL_FIELD =
                LocateBy.css("input#fw-device-model");

        /** Manufacturer optional text field */
        public static final String MANUFACTURER_FIELD =
                LocateBy.css("input#fw-manufacturer");

        /**
         * Release Date calendar popover trigger button.
         * This is NOT an input — use Click.on() to open the calendar; interact with
         * the calendar popover separately to select a date.
         */
        public static final String RELEASE_DATE_TRIGGER =
                LocateBy.css("[role='dialog'] [data-slot='popover-trigger']");

        /** Firmware file picker input (type="file") */
        public static final String FILE_INPUT =
                LocateBy.css("input#fw-file");

        /** Release Notes optional textarea */
        public static final String RELEASE_NOTES_FIELD =
                LocateBy.css("textarea#fw-release-notes");

        /**
         * Firmware Family label — expected AFTER PS-34 front-end update (TC-34.7.08).
         * This locator will NOT match the current UI — use in isNotVisible() checks to
         * confirm the gap, or in isVisible() checks post-PS-34.
         */
        public static final String FIRMWARE_FAMILY_LABEL =
                LocateBy.withCssText("[role='dialog'] label", "Firmware Family");

        /**
         * Firmware Family dropdown/select field — expected AFTER PS-34 front-end update.
         * Likely a select or combobox; exact selector may need updating once the UI ships.
         */
        public static final String FIRMWARE_FAMILY_FIELD =
                LocateBy.css("[role='dialog'] select#fw-family-id, [role='dialog'] [id='fw-family-id']");

        /**
         * Submit (Upload) button — HTML disabled until Name + Version + Device Model
         * + Firmware File are all non-empty.
         * Use browser.evaluate() or isNotVisible(disabled check) for disabled-state assertions.
         */
        public static final String SUBMIT_BUTTON =
                LocateBy.css("[role='dialog'] button[type='submit']");

        /** Cancel button — dismisses the modal without submitting */
        public static final String CANCEL_BUTTON =
                LocateBy.css("[data-slot='dialog-footer'] button[type='button']");

        /** Close X button (top-right corner of the modal) */
        public static final String CLOSE_BUTTON =
                LocateBy.css("[role='dialog'] button.absolute.top-5.right-5");
    }

    // ──────────────────────────── Audit Log Section ──────────────────────────

    /**
     * Locators for the Audit Log tab content on the Deployment page.
     *
     * DOM reality:
     *  - No role="tabpanel" on the audit content — it is a raw div.space-y-3
     *  - Entries: each is a div.bg-card inside the entries div.space-y-3
     *  - Action type badge: span.inline-flex with colour class
     *    Created → bg-green-100, Modified → bg-blue-100
     *  - Show/Hide changes toggle: button[aria-label="Show changes"|"Hide changes"]
     *  - Diff old value: span.text-red-600.line-through
     *  - Diff new value: span.text-green-600 (no line-through)
     *  - Resource ID: span.font-mono.text-muted-foreground.truncate
     *  - Export button: "Export Full Log" text
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class AuditLog {

        /** "Showing X of Y activities" summary row */
        public static final String SUMMARY_ROW =
                LocateBy.withCssText("div.bg-card", "Showing");

        /** Export Full Log button */
        public static final String EXPORT_BUTTON =
                LocateBy.withCssText("button", "Export Full Log");

        /** Individual audit entry cards */
        public static final String ENTRY_ITEM =
                LocateBy.css("main .space-y-3 .space-y-3 .bg-card");

        /** "Created" action badge — green bg-green-100 */
        public static final String CREATED_BADGE =
                LocateBy.css("main .space-y-3 .bg-card span.bg-green-100");

        /** "Modified" action badge — blue bg-blue-100 */
        public static final String MODIFIED_BADGE =
                LocateBy.css("main .space-y-3 .bg-card span.bg-blue-100");

        /** "Show changes" expand toggle — aria-expanded="false" */
        public static final String SHOW_CHANGES_BUTTON =
                LocateBy.css("button[aria-label='Show changes']");

        /** "Hide changes" collapse toggle — aria-expanded="true" */
        public static final String HIDE_CHANGES_BUTTON =
                LocateBy.css("button[aria-label='Hide changes']");

        /** Old (removed) field value in the diff — red strikethrough */
        public static final String DIFF_OLD_VALUE =
                LocateBy.css(".text-red-600.line-through");

        /** New (added) field value in the diff — green */
        public static final String DIFF_NEW_VALUE =
                LocateBy.css("span.text-green-600:not(.line-through)");

        /** Resource ID monospace span in an audit entry */
        public static final String RESOURCE_ID =
                LocateBy.css(".font-mono.text-muted-foreground.truncate");
    }
}
