package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the HLM Platform Inventory & Assets page.
 *
 * Locators verified via browser inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/inventory
 *
 * Key findings:
 *  - Page sub-heading (h2): "Inventory & Asset Management"
 *  - View tabs: "Hardware Inventory" | "Firmware Status" | "Geo Location"
 *  - Search input: type="text", placeholder contains "Search by device name..."
 *  - Device rows rendered inside a <tbody> when devices exist
 *  - No "Add Item" CRUD button – this is a read/monitor focused view
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryPage {

    // ──────────────── Page header / toolbar ────────────────

    /** Section heading – "Inventory & Asset Management" */
    public static final String PAGE_HEADING          = LocateBy.withCssText("h2", "Inventory & Asset Management");

    /** Search input – type=text, placeholder "Search by device name…" */
    public static final String SEARCH_FIELD          = LocateBy.css("input[type='text']");

    /** Hardware Inventory tab button */
    public static final String TAB_HARDWARE          = LocateBy.withExactCssText("button", "Hardware Inventory");

    /** Firmware Status tab button */
    public static final String TAB_FIRMWARE          = LocateBy.withExactCssText("button", "Firmware Status");

    /** Geo Location tab button */
    public static final String TAB_GEO               = LocateBy.withExactCssText("button", "Geo Location");

    /** Filter button next to the search bar */
    public static final String FILTER_BUTTON         = LocateBy.withExactCssText("button", "Filter");

    // ──────────────── Device table (Hardware Inventory view) ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class DeviceTable {

        /** Table body rows – present when devices are loaded */
        public static final String ALL_ROWS          = LocateBy.css("tbody tr");

        /** First row, first cell (device name) */
        public static final String FIRST_ROW_NAME    = LocateBy.css("tbody tr:first-child td:first-child");

        /** No-data / loading message shown when table is empty */
        public static final String LOADING_MSG       = LocateBy.withCssText("div", "Loading devices");

        /** Pagination label – "Page X of Y" */
        public static final String PAGINATION_LABEL  = LocateBy.withCssText("div", "Page");
    }

    // ──────────────── CRUD toolbar ────────────────

    /** Add Item button in the toolbar */
    public static final String ADD_ITEM_BUTTON       = LocateBy.withExactCssText("button", "Add Item");

    /** Success toast notification */
    public static final String SUCCESS_TOAST         = LocateBy.css("[role='alert']");

    /** Confirmation dialog for delete actions */
    public static final String CONFIRM_DIALOG        = LocateBy.css("[role='dialog']");

    /** Confirm delete button inside the confirmation dialog */
    public static final String CONFIRM_DELETE_BUTTON = LocateBy.withExactCssText("button", "Delete");

    /** Cancel delete button inside the confirmation dialog */
    public static final String CANCEL_DELETE_BUTTON  = LocateBy.withExactCssText("button", "Cancel");

    // ──────────────── Item form ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ItemForm {
        public static final String FORM_TITLE        = LocateBy.withCssText("h2", "Item");
        public static final String ITEM_NAME         = LocateBy.css("input[name='name']");
        public static final String SKU               = LocateBy.css("input[name='sku']");
        public static final String CATEGORY_DROPDOWN = LocateBy.css("select[name='category']");
        public static final String QUANTITY          = LocateBy.css("input[name='quantity']");
        public static final String UNIT_PRICE        = LocateBy.css("input[name='unitPrice']");
        public static final String DESCRIPTION       = LocateBy.css("textarea[name='description']");
        public static final String SUPPLIER          = LocateBy.css("input[name='supplier']");
        public static final String SAVE_BUTTON       = LocateBy.withExactCssText("button", "Save");
        public static final String CANCEL_BUTTON     = LocateBy.withExactCssText("button", "Cancel");
        public static final String ERROR_ITEM_NAME   = LocateBy.css("[data-error='name']");
        public static final String ERROR_SKU         = LocateBy.css("[data-error='sku']");
        public static final String ERROR_QUANTITY    = LocateBy.css("[data-error='quantity']");
    }

    // ──────────────── Item table (CRUD view) ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ItemTable {
        public static final String TABLE             = LocateBy.css("table");
        public static final String ALL_ROWS          = LocateBy.css("tbody tr");
        public static final String FIRST_ROW_NAME   = LocateBy.css("tbody tr:first-child td:first-child");
        public static final String EMPTY_STATE_MSG   = LocateBy.withCssText("div", "No items found");
        public static final String EDIT_BTN          = LocateBy.withExactCssText("button", "Edit");
        public static final String DELETE_BTN        = LocateBy.withExactCssText("button", "Delete");
    }

    // ──────────────── Stats counters ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Stats {
        public static final String TOTAL_DEVICES     = LocateBy.withCssText("div", "Total Devices");
        public static final String ONLINE            = LocateBy.withCssText("div", "Online");
        public static final String IN_MAINTENANCE    = LocateBy.withCssText("div", "In Maintenance");
        public static final String OFFLINE           = LocateBy.withCssText("div", "Offline");
    }
}
