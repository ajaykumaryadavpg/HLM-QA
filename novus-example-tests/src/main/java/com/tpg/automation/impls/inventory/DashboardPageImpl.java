package com.tpg.automation.impls.inventory;

import com.tpg.actions.Click;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.automation.pages.inventory.DashboardPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DashboardPageImpl {

    public static Performable goToInventoryModule() {
        return Perform.actions(
                Click.on(DashboardPage.NavMenu.INVENTORY_LINK)
        ).log("goToInventoryModule", "Navigates to the Inventory module via the nav menu");
    }

    public static Performable goToReportsModule() {
        return Perform.actions(
                Click.on(DashboardPage.NavMenu.REPORTS_LINK)
        ).log("goToReportsModule", "Navigates to the Reports module via the nav menu");
    }

    public static Performable clickLogout() {
        return Perform.actions(
                Click.on(DashboardPage.LOGOUT_BUTTON)
        ).log("clickLogout", "Clicks the Logout button from the dashboard");
    }

    public static Performable clickRefreshDashboard() {
        return Perform.actions(
                Click.on(DashboardPage.REFRESH_DASHBOARD_BUTTON)
        ).log("clickRefreshDashboard", "Clicks the Refresh Dashboard button (circular arrow icon, top-right)");
    }
}
