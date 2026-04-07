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

    // ──────────────── Firmware upload form ────────────────

    /**
     * Test data for the Deployment page Upload Firmware modal form (PS-34 / TC-34.7.07).
     * All constants represent valid values unless the nested class name indicates otherwise.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FirmwareForm {
        public static final String NAME         = "[AUTO-TEST] Solar Inverter Firmware";
        public static final String VERSION      = "v2.1.0-test";
        public static final String DEVICE_MODEL = "LN-11";
        public static final String MANUFACTURER = "Sungrow";
        public static final String RELEASE_NOTES = "Automated QA test upload — safe to delete";

        /** Family name used in createFirmwareFamily mutation tests (post-PS-34) */
        public static final String FAMILY_NAME  = "[AUTO-TEST] Solar Inverter Family";
    }

    /**
     * Expected firmware status enum values for the Deployment page.
     * Pre-PS-34 values are in the LEGACY nested class; post-PS-34 values in UPDATED.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FirmwareStatus {

        /** Status values currently in production (pre-PS-34 schema) */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Legacy {
            public static final String PENDING    = "Pending";
            public static final String APPROVED   = "Approved";
            public static final String DEPRECATED = "Deprecated";
            public static final String REJECTED   = "Rejected";
        }

        /**
         * Status values after PS-34 ships (expanded 5-value enum).
         * Tests using these constants should be marked {@code enabled = false}
         * until the PS-34 backend and front-end are deployed to the target environment.
         */
        @NoArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Updated {
            public static final String SCREENING  = "Screening";
            public static final String STAGED     = "Staged";
            public static final String ACTIVE     = "Active";
            public static final String DEPRECATED = "Deprecated";
            public static final String RECALLED   = "Recalled";
        }
    }

    // ──────────────── FirmwareFamily CRUD API (PS-35) ────────────────

    /**
     * Test data for the FirmwareFamily CRUD resolver API tests (PS-35 / PS-42).
     * Used by {@code FirmwareFamilyApiTests} and {@code FirmwareFamilyE2ETests}.
     */
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class FirmwareFamilyApi {

        /** GraphQL AppSync endpoint for the HLM Platform */
        public static final String GRAPHQL_ENDPOINT = "https://main.dddsig2mih3hw.amplifyapp.com/api/graphql";

        /** Valid family name for create / update tests */
        public static final String FAMILY_NAME         = "[AUTO-TEST] Solar Inverter Family";
        public static final String FAMILY_NAME_UPDATED  = "[AUTO-TEST] Solar Inverter Family v2";
        public static final String FAMILY_NAME_MINIMAL  = "[AUTO-TEST] Minimal Family";

        /** Target device models */
        public static final String TARGET_MODEL_1 = "SG-5K";
        public static final String TARGET_MODEL_2 = "SG-8K";
        public static final String TARGET_MODEL_SINGLE = "SG-3K";

        /** Valid status values */
        public static final String STATUS_ACTIVE     = "Active";
        public static final String STATUS_SCREENING  = "Screening";
        public static final String STATUS_STAGED     = "Staged";
        public static final String STATUS_DEPRECATED = "Deprecated";
        public static final String STATUS_RECALLED   = "Recalled";

        /** Invalid / edge-case values */
        public static final String INVALID_STATUS    = "InvalidStatus";
        public static final String EMPTY             = "";
        public static final String MAX_LENGTH_NAME   = "A".repeat(301);
        public static final String NON_EXISTENT_ID   = "ffffffff-ffff-ffff-ffff-ffffffffffff";
        public static final String MALFORMED_TOKEN   = "not-a-real-cursor-token";

        /** Expected error substrings */
        public static final String ERROR_UNAUTHORIZED  = "Unauthorized";
        public static final String ERROR_VALIDATION     = "validation";
        public static final String ERROR_NOT_FOUND      = "not found";
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
