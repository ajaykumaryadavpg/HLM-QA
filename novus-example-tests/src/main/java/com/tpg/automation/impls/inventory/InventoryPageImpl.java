package com.tpg.automation.impls.inventory;

import com.tpg.actions.Click;
import com.tpg.actions.Clear;
import com.tpg.actions.Enter;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.actions.Select;
import com.tpg.automation.pages.inventory.InventoryPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryPageImpl {

    // ──────────────── Toolbar actions ────────────────

    public static Performable clickAddItem() {
        return Perform.actions(
                Click.on(InventoryPage.ADD_ITEM_BUTTON)
        ).log("clickAddItem", "Clicks the Add Item button to open the item form");
    }

    public static Performable searchInventory(String searchTerm) {
        return Perform.actions(
                Enter.text(searchTerm).on(InventoryPage.SEARCH_FIELD)
        ).log("searchInventory", "Enters search term into the inventory search field");
    }

    public static Performable clearSearch() {
        return Perform.actions(
                Clear.locator(InventoryPage.SEARCH_FIELD)
        ).log("clearSearch", "Clears the inventory search field");
    }

    // ──────────────── Item form actions ────────────────

    public static Performable fillItemName(String name) {
        return Perform.actions(
                Enter.text(name).on(InventoryPage.ItemForm.ITEM_NAME)
        ).log("fillItemName", "Fills the item name on the item form");
    }

    public static Performable fillSku(String sku) {
        return Perform.actions(
                Enter.text(sku).on(InventoryPage.ItemForm.SKU)
        ).log("fillSku", "Fills the SKU on the item form");
    }

    public static Performable selectCategory(String category) {
        return Perform.actions(
                Select.option(category).on(InventoryPage.ItemForm.CATEGORY_DROPDOWN)
        ).log("selectCategory", "Selects the category from the dropdown on the item form");
    }

    public static Performable fillQuantity(String quantity) {
        return Perform.actions(
                Enter.text(quantity).on(InventoryPage.ItemForm.QUANTITY)
        ).log("fillQuantity", "Fills the quantity on the item form");
    }

    public static Performable fillUnitPrice(String unitPrice) {
        return Perform.actions(
                Enter.text(unitPrice).on(InventoryPage.ItemForm.UNIT_PRICE)
        ).log("fillUnitPrice", "Fills the unit price on the item form");
    }

    public static Performable fillDescription(String description) {
        return Perform.actions(
                Enter.text(description).on(InventoryPage.ItemForm.DESCRIPTION)
        ).log("fillDescription", "Fills the item description on the item form");
    }

    public static Performable fillSupplier(String supplier) {
        return Perform.actions(
                Enter.text(supplier).on(InventoryPage.ItemForm.SUPPLIER)
        ).log("fillSupplier", "Fills the supplier name on the item form");
    }

    public static Performable clickSaveItem() {
        return Perform.actions(
                Click.on(InventoryPage.ItemForm.SAVE_BUTTON)
        ).log("clickSaveItem", "Clicks the Save button to submit the item form");
    }

    public static Performable clickCancelForm() {
        return Perform.actions(
                Click.on(InventoryPage.ItemForm.CANCEL_BUTTON)
        ).log("clickCancelForm", "Clicks the Cancel button to dismiss the item form");
    }

    // ──────────────── Table row actions ────────────────

    public static Performable clickEditFirstItem() {
        return Perform.actions(
                Click.on(InventoryPage.ItemTable.EDIT_BTN).nth(0)
        ).log("clickEditFirstItem", "Clicks the Edit button on the first inventory table row");
    }

    public static Performable clickDeleteFirstItem() {
        return Perform.actions(
                Click.on(InventoryPage.ItemTable.DELETE_BTN).nth(0)
        ).log("clickDeleteFirstItem", "Clicks the Delete button on the first inventory table row");
    }

    // ──────────────── Confirmation dialog actions ────────────────

    public static Performable confirmDeletion() {
        return Perform.actions(
                Click.on(InventoryPage.CONFIRM_DELETE_BUTTON)
        ).log("confirmDeletion", "Confirms the item deletion in the confirmation dialog");
    }

    public static Performable cancelDeletion() {
        return Perform.actions(
                Click.on(InventoryPage.CANCEL_DELETE_BUTTON)
        ).log("cancelDeletion", "Cancels the item deletion from the confirmation dialog");
    }
}
