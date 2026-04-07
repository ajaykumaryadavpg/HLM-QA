package com.tpg.automation.impls.inventory;

import com.tpg.actions.Click;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.actions.Type;
import com.tpg.automation.pages.inventory.DashboardPage;
import com.tpg.automation.pages.inventory.DeploymentPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page implementation class for the HLM Platform Deployment page (/deployment).
 *
 * All methods return static {@link Performable} instances that can be passed to
 * {@code user.attemptsTo()} in test classes. Follows the framework convention of
 * {@code Perform.actions(...).log("methodName", "human-readable description")}.
 *
 * @see DeploymentPage
 * @jira PS-34
 * @jira PS-41 (QA Sub-task)
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DeploymentPageImpl {

    // ──────────────────────────── Navigation ─────────────────────────────────

    /**
     * Navigates to the Deployment page by clicking the sidebar nav link.
     * Equivalent to clicking "Deployment" in the left-hand navigation menu.
     */
    public static Performable goToDeploymentModule() {
        return Perform.actions(
                Click.on(DashboardPage.NavMenu.DEPLOYMENT_LINK)
        ).log("goToDeploymentModule", "Navigates to the Deployment module via the sidebar nav link");
    }

    // ──────────────────────────── Tab Switching ───────────────────────────────

    /**
     * Clicks the "Firmware Versions" tab to activate the firmware cards panel.
     * Use when the Audit Log tab is currently active and firmware cards need to be visible.
     */
    public static Performable clickFirmwareVersionsTab() {
        return Perform.actions(
                Click.on(DeploymentPage.FIRMWARE_VERSIONS_TAB)
        ).log("clickFirmwareVersionsTab", "Clicks the 'Firmware Versions' tab to show firmware cards");
    }

    /**
     * Clicks the "Audit Log" tab to switch the content panel to the deployment audit entries.
     */
    public static Performable clickAuditLogTab() {
        return Perform.actions(
                Click.on(DeploymentPage.AUDIT_LOG_TAB)
        ).log("clickAuditLogTab", "Clicks the 'Audit Log' tab to show deployment audit entries");
    }

    // ──────────────────────────── Search ─────────────────────────────────────

    /**
     * Types a search term into the firmware / audit-log search input.
     *
     * @param searchTerm text to search for (firmware name, version, model; or audit resource/action)
     */
    public static Performable searchFor(String searchTerm) {
        return Perform.actions(
                Type.text(searchTerm).on(DeploymentPage.SEARCH_INPUT)
        ).log("searchFor", "Types '" + searchTerm + "' into the Deployment page search field");
    }

    // ──────────────────────────── Upload Firmware Modal ──────────────────────

    /**
     * Clicks the "Upload Firmware" button to open the upload modal dialog.
     */
    public static Performable clickUploadFirmwareButton() {
        return Perform.actions(
                Click.on(DeploymentPage.UPLOAD_FIRMWARE_BUTTON)
        ).log("clickUploadFirmwareButton", "Clicks the 'Upload Firmware' button to open the upload dialog");
    }

    /**
     * Types the firmware name into the Name field inside the Upload Firmware modal.
     *
     * @param name firmware name value
     */
    public static Performable fillFirmwareName(String name) {
        return Perform.actions(
                Type.text(name).on(DeploymentPage.UploadModal.NAME_FIELD)
        ).log("fillFirmwareName", "Types '" + name + "' into the fw-name field in the Upload Firmware modal");
    }

    /**
     * Types the firmware version into the Version field inside the Upload Firmware modal.
     *
     * @param version version string (e.g., "v2.1.0")
     */
    public static Performable fillFirmwareVersion(String version) {
        return Perform.actions(
                Type.text(version).on(DeploymentPage.UploadModal.VERSION_FIELD)
        ).log("fillFirmwareVersion", "Types '" + version + "' into the fw-version field in the Upload Firmware modal");
    }

    /**
     * Types the device model into the Device Model field inside the Upload Firmware modal.
     *
     * @param model device model identifier (e.g., "SRV-9000")
     */
    public static Performable fillDeviceModel(String model) {
        return Perform.actions(
                Type.text(model).on(DeploymentPage.UploadModal.DEVICE_MODEL_FIELD)
        ).log("fillDeviceModel", "Types '" + model + "' into the fw-device-model field in the Upload Firmware modal");
    }

    /**
     * Types the manufacturer name into the optional Manufacturer field.
     *
     * @param manufacturer manufacturer name (optional field)
     */
    public static Performable fillManufacturer(String manufacturer) {
        return Perform.actions(
                Type.text(manufacturer).on(DeploymentPage.UploadModal.MANUFACTURER_FIELD)
        ).log("fillManufacturer", "Types '" + manufacturer + "' into the optional fw-manufacturer field");
    }

    /**
     * Types release notes into the optional Release Notes textarea.
     *
     * @param notes release notes text (optional field)
     */
    public static Performable fillReleaseNotes(String notes) {
        return Perform.actions(
                Type.text(notes).on(DeploymentPage.UploadModal.RELEASE_NOTES_FIELD)
        ).log("fillReleaseNotes", "Types release notes into the optional fw-release-notes textarea");
    }

    /**
     * Clicks the Submit button inside the Upload Firmware modal.
     * Only effective when the button is enabled (all required fields populated).
     */
    public static Performable clickSubmitUpload() {
        return Perform.actions(
                Click.on(DeploymentPage.UploadModal.SUBMIT_BUTTON)
        ).log("clickSubmitUpload", "Clicks the Submit button in the Upload Firmware modal");
    }

    /**
     * Clicks the Cancel button to dismiss the Upload Firmware modal without submitting.
     */
    public static Performable clickCancelUpload() {
        return Perform.actions(
                Click.on(DeploymentPage.UploadModal.CANCEL_BUTTON)
        ).log("clickCancelUpload", "Clicks Cancel in the Upload Firmware modal to dismiss without submitting");
    }

    /**
     * Clicks the Close (X) button to dismiss the Upload Firmware modal.
     */
    public static Performable clickCloseModal() {
        return Perform.actions(
                Click.on(DeploymentPage.UploadModal.CLOSE_BUTTON)
        ).log("clickCloseModal", "Clicks the Close (X) button on the Upload Firmware modal");
    }

    // ──────────────────────────── Firmware Card Actions ──────────────────────

    /**
     * Clicks the Details toggle button on the first firmware card to expand inline details.
     * The button toggles aria-expanded from "false" to "true".
     */
    public static Performable clickDetailsToggleOnFirstCard() {
        return Perform.actions(
                Click.on(DeploymentPage.FirmwareCard.DETAILS_TOGGLE).nth(0)
        ).log("clickDetailsToggleOnFirstCard", "Clicks the Details expand button on the first firmware card");
    }

    /**
     * Clicks the Audit Log "Show changes" button on the first audit entry to expand the diff.
     */
    public static Performable clickShowChangesOnFirstAuditEntry() {
        return Perform.actions(
                Click.on(DeploymentPage.AuditLog.SHOW_CHANGES_BUTTON).nth(0)
        ).log("clickShowChangesOnFirstAuditEntry", "Clicks 'Show changes' on the first audit log entry to reveal field diff");
    }
}
