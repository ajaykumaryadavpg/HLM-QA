package com.tpg.automation.testdata;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Centralised test-data constants for Inventory Management tests.
 *
 * <p>Values are kept here so they are easy to update without touching test files.
 * For environment-specific credentials, prefer externalising to properties files
 * and injecting via {@code @Value}; these constants serve as safe defaults for
 * local development and CI runs against a known test environment.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class InventoryTestData {

    // ──────────────── Credentials ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Credentials {
        /** Valid admin account for the HLM Platform */
        public static final String ADMIN_USERNAME   = "ajaykumar.yadav@3pillarglobal.com";
        public static final String ADMIN_PASSWORD   = "Secure@12345";
        /** Deliberately wrong credentials for negative tests */
        public static final String INVALID_USERNAME = "invalid.user@test.com";
        public static final String INVALID_PASSWORD = "WrongPass!99";
        public static final String EMPTY            = "";
    }

    // ──────────────── Inventory item – valid ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class ValidItem {
        public static final String NAME        = "Wireless Keyboard";
        public static final String SKU         = "WK-2024-BLK";
        public static final String CATEGORY    = "Electronics";
        public static final String QUANTITY    = "50";
        public static final String UNIT_PRICE  = "29.99";
        public static final String DESCRIPTION = "Ergonomic wireless keyboard with USB receiver";
        public static final String SUPPLIER    = "TechSupply Co.";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UpdatedItem {
        public static final String NAME        = "Wireless Keyboard Pro";
        public static final String QUANTITY    = "75";
        public static final String UNIT_PRICE  = "34.99";
    }

    // ──────────────── Inventory item – edge cases ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class EdgeCase {
        public static final String NEGATIVE_QUANTITY      = "-5";
        public static final String ZERO_QUANTITY          = "0";
        public static final String ALPHA_QUANTITY         = "abc";
        public static final String SPECIAL_CHAR_NAME      = "@#$%^&*";
        public static final String MAX_LENGTH_NAME        = "A".repeat(256);
    }

    // ──────────────── Categories ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Categories {
        public static final String ELECTRONICS = "Electronics";
        public static final String FURNITURE   = "Furniture";
        public static final String STATIONERY  = "Stationery";
        public static final String TOOLS       = "Tools";
    }

    // ──────────────── Expected messages ────────────────

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Messages {
        public static final String LOGIN_ERROR          = "Invalid username or password";
        public static final String ITEM_ADDED           = "Item added successfully";
        public static final String ITEM_UPDATED         = "Item updated successfully";
        public static final String ITEM_DELETED         = "Item deleted successfully";
        public static final String REQUIRED_FIELD_ERROR = "This field is required";
        public static final String INVALID_QUANTITY     = "Quantity must be a positive number";
        public static final String NO_RESULTS_FOUND     = "No items found";
    }
}
