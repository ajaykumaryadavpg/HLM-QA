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
}
