package com.tpg.automation.inventory;

import com.tpg.actions.Waiting;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DeploymentPageImpl;
import com.tpg.automation.macros.Navigate;
import com.tpg.automation.pages.inventory.DeploymentPage;
import com.tpg.automation.pages.inventory.DeploymentPage.UploadModal;
import com.tpg.automation.testdata.InventoryTestData.FirmwareForm;
import com.tpg.verification.Verify;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.automation.constants.TestGroups.UPLOAD_FIRMWARE;

/**
 * Test suite for the Upload Firmware modal form on the Deployment page (Story PS-34 / AC-7).
 *
 * <p>Covers:
 * <ul>
 *   <li>TC-34.7.07 — Upload Firmware form validates required fields; Submit button stays
 *       disabled until Name, Version, Device Model, and Firmware File are all provided.
 *   <li>TC-34.7.08 — Gap check: the "Firmware Family" dropdown is expected in the modal
 *       after PS-34 front-end work ships, but is currently absent. The test is preserved
 *       as {@code enabled = false} and will be activated once the UI is updated.
 * </ul>
 *
 * <p>Tests in this class open the Upload Firmware modal individually as required. The
 * {@code @BeforeMethod} navigates to the Deployment page only; each test that requires
 * the modal open calls {@link DeploymentPageImpl#clickUploadFirmwareButton()} explicitly.
 *
 * <p>File input handling: the Submit button's enabled state is controlled by JavaScript
 * (not HTML5 {@code required}). To test the "all-fields-filled" scenario the test creates
 * a transient temp file via {@link Files#createTempFile} and sets it on {@code input#fw-file}
 * using the Playwright {@code Page.setInputFiles()} API available directly on {@code browser}.
 *
 * @see DeploymentPage
 * @see DeploymentPageImpl
 * @see InventoryTestBase
 * @jira PS-34 (Story: FirmwareFamily DynamoDB Entity &amp; AppSync Schema)
 * @jira PS-41 (QA Sub-task)
 */
public class UploadFirmwareTests extends InventoryTestBase {

    // ──────────────────────────── Pre-test Setup ─────────────────────────────

    /**
     * Navigate to the Deployment page before each test.
     * Individual tests open the modal when required.
     */
    @BeforeMethod(alwaysRun = true)
    public void navigateToDeploymentPage() {
        step("Navigate to Deployment module via Dashboard sidebar nav link");
        user.attemptsTo(Navigate.to().deploymentPage());

        step("Wait for Upload Firmware button to be available");
        user.is(Waiting.on(DeploymentPage.UPLOAD_FIRMWARE_BUTTON).within(15));
    }

    // ── TC-34.7.07-001 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-001",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify the Upload Firmware button is visible on the Deployment page")
    @Outcome("'Upload Firmware' button (btn-primary) is visible at the top of the Deployment page")
    @Test(groups = {SMOKE_TESTS, UPLOAD_FIRMWARE})
    public void testUploadFirmwareButtonIsVisibleOnDeploymentPage() {

        step("Verify Upload Firmware button is visible");
        user.wantsTo(
                Verify.uiElement(DeploymentPage.UPLOAD_FIRMWARE_BUTTON)
                        .describedAs("button:has-text('Upload Firmware') is visible on the Deployment page")
                        .isVisible()
        );
    }

    // ── TC-34.7.07-002 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-002",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify clicking 'Upload Firmware' opens the modal dialog with the correct title and all expected form fields")
    @Outcome("Modal opens with title 'Upload Firmware'; Name, Version, Device Model, Manufacturer, File, and Release Notes fields are visible")
    @Test(groups = {SMOKE_TESTS, UPLOAD_FIRMWARE})
    public void testUploadFirmwareModalOpensWithAllFormFields() {

        step("Click Upload Firmware button to open the modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());

        step("Wait for the modal dialog to appear");
        user.is(Waiting.on(UploadModal.CONTAINER).within(10));

        step("Verify the modal title is 'Upload Firmware'");
        user.wantsTo(
                Verify.uiElement(UploadModal.TITLE)
                        .describedAs("[data-slot='dialog-title'] is visible with text 'Upload Firmware'")
                        .isVisible()
        );

        step("Verify required Name field is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.NAME_FIELD)
                        .describedAs("input#fw-name is visible in the modal")
                        .isVisible()
        );

        step("Verify required Version field is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.VERSION_FIELD)
                        .describedAs("input#fw-version is visible in the modal")
                        .isVisible()
        );

        step("Verify required Device Model field is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.DEVICE_MODEL_FIELD)
                        .describedAs("input#fw-device-model is visible in the modal")
                        .isVisible()
        );

        step("Verify optional Manufacturer field is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.MANUFACTURER_FIELD)
                        .describedAs("input#fw-manufacturer is visible in the modal")
                        .isVisible()
        );

        step("Verify file picker input is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.FILE_INPUT)
                        .describedAs("input#fw-file (type=file) is visible in the modal")
                        .isVisible()
        );

        step("Verify optional Release Notes textarea is visible");
        user.wantsTo(
                Verify.uiElement(UploadModal.RELEASE_NOTES_FIELD)
                        .describedAs("textarea#fw-release-notes is visible in the modal")
                        .isVisible()
        );

        step("Close the modal via the Cancel button");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }

    // ── TC-34.7.07-003 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-003",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify Submit button is disabled when all form fields are empty")
    @Outcome("button[type='submit'] is present but has the disabled HTML attribute; form cannot be submitted with no data")
    @Test(groups = {SMOKE_TESTS, UPLOAD_FIRMWARE})
    public void testSubmitButtonIsDisabledWhenAllFieldsAreEmpty() {

        step("Click Upload Firmware button to open the modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());

        step("Wait for the modal to appear");
        user.is(Waiting.on(UploadModal.SUBMIT_BUTTON).within(10));

        step("Verify Submit button is visible but disabled (all fields empty)");
        user.wantsTo(
                Verify.uiElement(UploadModal.SUBMIT_BUTTON)
                        .describedAs("button[type='submit'] is present in the modal")
                        .isVisible()
        );

        step("Assert Submit button is disabled via JS evaluation — all required fields are empty");
        boolean isDisabled = (Boolean) browser.evaluate(
                "() => document.querySelector('[role=\"dialog\"] button[type=\"submit\"]').disabled"
        );
        if (!isDisabled) {
            throw new AssertionError(
                    "TC-34.7.07-003 FAILED: Submit button is enabled with all fields empty — " +
                    "expected disabled=true but got disabled=false"
            );
        }
        log.step("Submit button is correctly disabled when all form fields are empty");

        step("Close the modal via Cancel");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }

    // ── TC-34.7.07-004 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-004",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify Submit button remains disabled when only the Name field is filled")
    @Outcome("Submit button stays disabled after Name is entered; Version, Device Model, and File are still empty")
    @Test(groups = {REGRESSION, UPLOAD_FIRMWARE})
    public void testSubmitButtonRemainsDisabledWithOnlyNameFilled() {

        step("Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.NAME_FIELD).within(10));

        step("Fill in only the Name field");
        user.attemptsTo(DeploymentPageImpl.fillFirmwareName(FirmwareForm.NAME));

        step("Assert Submit button is still disabled — Version, Device Model, File are empty");
        boolean isDisabled = (Boolean) browser.evaluate(
                "() => document.querySelector('[role=\"dialog\"] button[type=\"submit\"]').disabled"
        );
        if (!isDisabled) {
            throw new AssertionError(
                    "TC-34.7.07-004 FAILED: Submit button is enabled with only Name filled — " +
                    "expected disabled=true but got disabled=false"
            );
        }
        log.step("Submit button remains disabled when only Name is provided");

        step("Close the modal via Cancel");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }

    // ── TC-34.7.07-005 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-005",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify Submit button remains disabled when Name, Version, and Device Model are filled but no file is attached")
    @Outcome("Submit button is still disabled without a firmware file; all three text fields are filled")
    @Test(groups = {REGRESSION, UPLOAD_FIRMWARE})
    public void testSubmitButtonRemainsDisabledWithoutFirmwareFile() {

        step("Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.NAME_FIELD).within(10));

        step("Fill in Name, Version, and Device Model — leave file picker empty");
        user.attemptsTo(DeploymentPageImpl.fillFirmwareName(FirmwareForm.NAME));
        user.attemptsTo(DeploymentPageImpl.fillFirmwareVersion(FirmwareForm.VERSION));
        user.attemptsTo(DeploymentPageImpl.fillDeviceModel(FirmwareForm.DEVICE_MODEL));

        step("Assert Submit button is still disabled — no firmware file attached");
        boolean isDisabled = (Boolean) browser.evaluate(
                "() => document.querySelector('[role=\"dialog\"] button[type=\"submit\"]').disabled"
        );
        if (!isDisabled) {
            throw new AssertionError(
                    "TC-34.7.07-005 FAILED: Submit button is enabled without a firmware file — " +
                    "expected disabled=true but got disabled=false"
            );
        }
        log.step("Submit button remains disabled when Name + Version + Device Model are filled but no file selected");

        step("Close the modal via Cancel");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }

    // ── TC-34.7.07-006 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-006",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify Submit button becomes enabled once all required fields (Name, Version, Device Model, File) are provided")
    @Outcome("Submit button transitions from disabled to enabled after a firmware file is attached and all text fields are filled")
    @Test(groups = {REGRESSION, UPLOAD_FIRMWARE})
    public void testSubmitButtonBecomesEnabledWhenAllRequiredFieldsAreFilled() throws IOException {

        step("Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.NAME_FIELD).within(10));

        step("Fill in all required text fields: Name, Version, Device Model");
        user.attemptsTo(DeploymentPageImpl.fillFirmwareName(FirmwareForm.NAME));
        user.attemptsTo(DeploymentPageImpl.fillFirmwareVersion(FirmwareForm.VERSION));
        user.attemptsTo(DeploymentPageImpl.fillDeviceModel(FirmwareForm.DEVICE_MODEL));

        step("Create a temporary firmware binary file and attach it via the file picker");
        Path tempFirmwareFile = Files.createTempFile("test-firmware-", ".bin");
        Files.write(tempFirmwareFile, new byte[]{0x7F, 0x45, 0x4C, 0x46}); // minimal ELF header bytes
        try {
            browser.setInputFiles("input#fw-file", tempFirmwareFile);

            step("Assert Submit button is now enabled — all required fields are populated");
            boolean isDisabled = (Boolean) browser.evaluate(
                    "() => document.querySelector('[role=\"dialog\"] button[type=\"submit\"]').disabled"
            );
            if (isDisabled) {
                throw new AssertionError(
                        "TC-34.7.07-006 FAILED: Submit button is still disabled after all required fields " +
                        "(Name, Version, Device Model, File) were provided — expected disabled=false"
                );
            }
            log.step("Submit button is correctly enabled once all required fields are populated");

        } finally {
            step("Close the modal via Cancel (cleanup — do not submit real data to backend)");
            user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
            Files.deleteIfExists(tempFirmwareFile);
        }
    }

    // ── TC-34.7.07-007 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.07-007",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("Verify clicking the Cancel button closes the Upload Firmware modal without submitting")
    @Outcome("Modal dialog is no longer visible after clicking Cancel; Deployment page remains active")
    @Test(groups = {SMOKE_TESTS, UPLOAD_FIRMWARE})
    public void testCancelButtonClosesModalWithoutSubmitting() {

        step("Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.CONTAINER).within(10));

        step("Verify modal is open");
        user.wantsTo(
                Verify.uiElement(UploadModal.CONTAINER)
                        .describedAs("[role='dialog'] is visible after clicking Upload Firmware")
                        .isVisible()
        );

        step("Click Cancel to dismiss the modal");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());

        step("Verify modal is no longer visible after Cancel");
        user.wantsTo(
                Verify.uiElement(UploadModal.CONTAINER)
                        .describedAs("[role='dialog'] is NOT visible after clicking Cancel")
                        .isNotVisible()
        );

        step("Verify Upload Firmware button is still visible — user remains on the Deployment page");
        user.wantsTo(
                Verify.uiElement(DeploymentPage.UPLOAD_FIRMWARE_BUTTON)
                        .describedAs("Upload Firmware button is still visible — modal closed cleanly, no navigation occurred")
                        .isVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // TC-34.7.08 — Gap check: Firmware Family dropdown (enabled = false)
    // This test FAILS on the current UI because the familyId / Firmware Family
    // field does not yet exist in the Upload Firmware form.
    // Enable after the PS-34 front-end work ships the dropdown to /deployment.
    // ══════════════════════════════════════════════════════════════════════════

    // ── TC-34.7.08-001 (post-PS-34 front-end) ───────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.08-001",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("[POST-PS-34 UI] Gap check — Verify the Upload Firmware form exposes a 'Firmware Family' " +
            "dropdown after the PS-34 front-end update. Currently FAILS (field not in DOM) and is kept " +
            "as a reminder that AC-3 UI exposure is incomplete.")
    @Outcome("A 'Firmware Family' label and a corresponding select / combobox are visible in the modal. " +
            "Selecting a family and submitting populates familyId on the created Firmware record.")
    @Test(groups = {REGRESSION, UPLOAD_FIRMWARE}, enabled = false)
    public void testPostPs34FirmwareFamilyDropdownIsExposedInUploadForm() {

        step("[POST-PS-34 UI] Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.CONTAINER).within(10));

        step("[POST-PS-34 UI] Verify 'Firmware Family' label is visible in the modal form");
        user.wantsTo(
                Verify.uiElement(UploadModal.FIRMWARE_FAMILY_LABEL)
                        .describedAs("label:has-text('Firmware Family') is visible in the Upload Firmware modal — " +
                                "confirms AC-3 familyId field has been added to the UI")
                        .isVisible()
        );

        step("[POST-PS-34 UI] Verify the Firmware Family dropdown/combobox is present and interactable");
        user.wantsTo(
                Verify.uiElement(UploadModal.FIRMWARE_FAMILY_FIELD)
                        .describedAs("Firmware Family select/combobox (id='fw-family-id') is visible and enabled")
                        .isVisible()
        );

        step("[POST-PS-34 UI] Close the modal without submitting");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }

    // ── TC-34.7.08-002 (post-PS-34 front-end) ───────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.7.08-002",
            stories = {"PS-34", "PS-41"}, category = "UPLOAD_FIRMWARE")
    @Description("[POST-PS-34 UI] Baseline check — Confirm the Firmware Family field is currently ABSENT " +
            "from the Upload Firmware form (documents the pre-PS-34 gap for AC-3)")
    @Outcome("'Firmware Family' label is NOT visible in the current modal — gap against AC-3 confirmed")
    @Test(groups = {REGRESSION, UPLOAD_FIRMWARE})
    public void testFirmwareFamilyFieldIsCurrentlyAbsentFromUploadForm() {

        step("Open the Upload Firmware modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());
        user.is(Waiting.on(UploadModal.CONTAINER).within(10));

        step("Verify 'Firmware Family' label is NOT visible — gap against AC-3 (PS-34) confirmed");
        user.wantsTo(
                Verify.uiElement(UploadModal.FIRMWARE_FAMILY_LABEL)
                        .describedAs("label:has-text('Firmware Family') is NOT visible in current UI — " +
                                "PS-34 AC-3 front-end work not yet shipped; familyId field absent from Upload Firmware form")
                        .isNotVisible()
        );

        step("Close the modal via Cancel");
        user.attemptsTo(DeploymentPageImpl.clickCancelUpload());
    }
}
