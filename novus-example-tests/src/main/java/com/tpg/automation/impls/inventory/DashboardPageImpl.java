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

    public static Performable clickViewAllAlerts() {
        return Perform.actions(
                Click.on(DashboardPage.AlertsPanel.VIEW_ALL_LINK)
        ).log("clickViewAllAlerts", "Clicks the 'View all' link in the Recent Alerts panel to navigate to /analytics");
    }

    // ──────────────────────── Quick Action Card Navigation (Story PS-6) ───────────────────────

    public static Performable clickViewInventoryCard() {
        return Perform.actions(
                Click.on(DashboardPage.QuickActions.VIEW_INVENTORY_CARD_MAIN)
        ).log("clickViewInventoryCard", "Clicks the 'View Inventory' quick action card (main content area) — navigates to /inventory");
    }

    public static Performable clickScheduleServiceCard() {
        return Perform.actions(
                Click.on(DashboardPage.QuickActions.SCHEDULE_SERVICE_CARD_MAIN)
        ).log("clickScheduleServiceCard", "Clicks the 'Schedule Service' quick action card (main content area) — navigates to /account-service");
    }

    public static Performable clickDeployFirmwareCard() {
        return Perform.actions(
                Click.on(DashboardPage.QuickActions.DEPLOY_FIRMWARE_CARD)
        ).log("clickDeployFirmwareCard", "Clicks the 'Deploy Firmware' quick action card (.relative.bg-card selector) — navigates to /deployment");
    }

    public static Performable clickCheckComplianceCard() {
        return Perform.actions(
                Click.on(DashboardPage.QuickActions.CHECK_COMPLIANCE_CARD_MAIN)
        ).log("clickCheckComplianceCard", "Clicks the 'Check Compliance' quick action card (main content area) — navigates to /compliance");
    }

    public static Performable goToDeploymentModule() {
        return Perform.actions(
                Click.on(DashboardPage.NavMenu.DEPLOYMENT_LINK)
        ).log("goToDeploymentModule", "Navigates to the Deployment module via the sidebar nav link");
    }
}
