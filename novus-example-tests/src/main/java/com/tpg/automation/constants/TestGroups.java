package com.tpg.automation.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestGroups {
    public static final String SMOKE_TESTS          = "smoke-tests";
    public static final String REGRESSION           = "regression";
    public static final String INVENTORY_LOGIN      = "inventory-login";
    public static final String INVENTORY_CRUD       = "inventory-crud";
    public static final String INVENTORY_SEARCH     = "inventory-search";
    public static final String NEGATIVE             = "negative";
    public static final String DASHBOARD_API        = "dashboard-api";
    public static final String DASHBOARD_KPI        = "dashboard-kpi";
    public static final String GEO_LOCATION           = "geo-location";
    public static final String DASHBOARD_ALERTS          = "dashboard-alerts";
    public static final String DASHBOARD_SYSTEM_STATUS   = "dashboard-system-status";
    public static final String DASHBOARD_QUICK_ACTIONS   = "dashboard-quick-actions";
    public static final String DASHBOARD_E2E_INTEGRATION = "dashboard-e2e-integration";
    public static final String FIRMWARE_STATUS           = "firmware-status";
    public static final String UPLOAD_FIRMWARE           = "upload-firmware";
    public static final String FIRMWARE_FAMILY_API       = "firmware-family-api";
    public static final String FIRMWARE_FAMILY_E2E       = "firmware-family-e2e";
}
