package com.tpg.automation.macros;

import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.tpg.automation.impls.home.HomePageImpl.goToContactPage;
import static com.tpg.automation.impls.inventory.DashboardPageImpl.goToDeploymentModule;
import static com.tpg.automation.impls.inventory.DashboardPageImpl.goToInventoryModule;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Navigate {

    public static Navigate to() {
        return new Navigate();
    }

    public Performable contactPage() {
        return Perform.actions(goToContactPage()).log("Navigate#contactPage", "navigating to the Contact landing page");
    }

    public Performable inventoryPage() {
        return Perform.actions(goToInventoryModule()).log("Navigate#inventoryPage", "navigating to the Inventory module");
    }

    public Performable deploymentPage() {
        return Perform.actions(goToDeploymentModule()).log("Navigate#deploymentPage", "navigating to the Deployment module");
    }
}
