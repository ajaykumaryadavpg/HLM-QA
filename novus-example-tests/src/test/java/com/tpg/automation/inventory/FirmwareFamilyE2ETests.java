package com.tpg.automation.inventory;

import com.tpg.actions.Launch;
import com.tpg.actions.Waiting;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.base.InventoryTestBase;
import com.tpg.automation.impls.inventory.DeploymentPageImpl;
import com.tpg.automation.macros.Navigate;
import com.tpg.automation.pages.inventory.DeploymentPage;
import com.tpg.automation.pages.inventory.DeploymentPage.FirmwareCard;
import com.tpg.automation.pages.inventory.DeploymentPage.UploadModal;
import com.tpg.automation.testdata.InventoryTestData.FirmwareFamilyApi;
import com.tpg.methods.Post;
import com.tpg.verification.Verify;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.FIRMWARE_FAMILY_E2E;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;
import static com.tpg.utils.CodeFillers.on;

/**
 * End-to-End CRUD flow + UI verification tests for FirmwareFamily (Story PS-35 / QA Sub-task PS-42).
 *
 * <p>Suite 9 from the test plan — validates that FirmwareFamily CRUD operations
 * performed via the API are correctly reflected in the Deployment page UI, and that
 * the Upload Firmware form exposes the FirmwareFamily association field.
 *
 * <p>These tests combine API calls (GraphQL mutations) with UI assertions (Playwright)
 * to verify the full stack integration end-to-end.
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class.
 * Each test method re-navigates to the Deployment page via {@code @BeforeMethod}.
 *
 * @see DeploymentPage
 * @see DeploymentPageImpl
 * @see InventoryTestBase
 * @jira PS-35 (Story: FirmwareFamily CRUD Resolvers &amp; API)
 * @jira PS-42 (QA Sub-task)
 */
public class FirmwareFamilyE2ETests extends InventoryTestBase {

    /** ID of the FirmwareFamily created during the E2E lifecycle test */
    private static String e2eFamilyId;

    // ──────────────────────────── Pre-test Setup ─────────────────────────────

    /**
     * Navigate to the Deployment page and wait for the Firmware Versions tab before each test.
     */
    @BeforeMethod(alwaysRun = true)
    public void navigateToDeploymentPage() {
        step("Navigate to Dashboard (clean state)");
        user.attemptsTo(
                Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions())
        );

        step("Navigate to Deployment module via Dashboard sidebar nav link");
        user.attemptsTo(Navigate.to().deploymentPage());

        step("Wait for Firmware Versions tab to be available");
        user.is(Waiting.on(DeploymentPage.FIRMWARE_VERSIONS_TAB).within(15));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 9 — End-to-End CRUD Flow + UI Verification
    // ══════════════════════════════════════════════════════════════════════════

    // ── TC-35.9.01 ─────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-35.9.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_E2E")
    @Description("Full CRUD lifecycle — create, read, update, read-again via API")
    @Outcome("FirmwareFamily is created, read back with correct data, updated, and read-again "
            + "with the updated values — all via GraphQL API calls")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_E2E}, priority = 1)
    public void testFullCrudLifecycleViaApi() {

        // ── CREATE ──────────────────────────────────────────────────────────
        step("Create a new FirmwareFamily via GraphQL mutation");
        String createMutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"%s\\", \\"%s\\"], \
                status: \\"%s\\" \
                }) { id familyName targetModels status createdAt } }"}"""
                .formatted(
                        FirmwareFamilyApi.FAMILY_NAME,
                        FirmwareFamilyApi.TARGET_MODEL_1,
                        FirmwareFamilyApi.TARGET_MODEL_2,
                        FirmwareFamilyApi.STATUS_ACTIVE
                );

        var createResponse = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(createMutation)
                .execute();

        step("Verify create response is OK and contains the familyName");
        createResponse.isOk();
        createResponse.bodyContains(FirmwareFamilyApi.FAMILY_NAME);

        step("Extract created family ID for subsequent operations");
        String createBody = createResponse.getContent();
        e2eFamilyId = extractJsonValue(createBody, "id");
        Assert.assertNotNull(e2eFamilyId, "Failed to extract family ID from create response");
        log.info("E2E Created FirmwareFamily ID: " + e2eFamilyId);

        // ── READ ────────────────────────────────────────────────────────────
        step("Read the created FirmwareFamily via getFirmwareFamily query");
        String getQuery = """
                {"query": "{ getFirmwareFamily(id: \\"%s\\") { id familyName targetModels status } }"}"""
                .formatted(e2eFamilyId);

        var getResponse = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(getQuery)
                .execute();

        step("Verify read response is OK and contains correct data");
        getResponse.isOk();
        getResponse.bodyContains(FirmwareFamilyApi.FAMILY_NAME);
        getResponse.bodyContains(FirmwareFamilyApi.TARGET_MODEL_1);
        getResponse.bodyContains(FirmwareFamilyApi.STATUS_ACTIVE);

        // ── UPDATE ──────────────────────────────────────────────────────────
        step("Update the FirmwareFamily name and status via mutation");
        String updateMutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"%s\\", \
                status: \\"%s\\" \
                }) { id familyName status updatedAt } }"}"""
                .formatted(e2eFamilyId, FirmwareFamilyApi.FAMILY_NAME_UPDATED, FirmwareFamilyApi.STATUS_DEPRECATED);

        var updateResponse = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(updateMutation)
                .execute();

        step("Verify update response is OK and contains the updated values");
        updateResponse.isOk();
        updateResponse.bodyContains(FirmwareFamilyApi.FAMILY_NAME_UPDATED);
        updateResponse.bodyContains(FirmwareFamilyApi.STATUS_DEPRECATED);

        // ── READ-AGAIN ──────────────────────────────────────────────────────
        step("Read the FirmwareFamily again to verify update persisted");
        var readAgain = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(getQuery)
                .execute();

        step("Verify read-again response contains updated familyName");
        readAgain.isOk();
        readAgain.bodyContains(FirmwareFamilyApi.FAMILY_NAME_UPDATED);

        step("Verify read-again response contains updated status");
        readAgain.bodyContains(FirmwareFamilyApi.STATUS_DEPRECATED);
    }

    // ── TC-35.9.02 ─────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-35.9.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_E2E")
    @Description("FirmwareFamily listing appears in UI after creation")
    @Outcome("After creating a FirmwareFamily via API, refreshing the Deployment page shows "
            + "firmware cards — the UI reflects backend data")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_E2E}, priority = 2)
    public void testFirmwareFamilyListingAppearsInUiAfterCreation() {

        step("Create a FirmwareFamily via API with a unique name for UI verification");
        String uniqueName = "[AUTO-TEST] UI Verify " + System.currentTimeMillis();
        String createMutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"SG-UI-TEST\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}"""
                .formatted(uniqueName);

        var createResponse = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(createMutation)
                .execute();

        step("Verify create response is OK");
        createResponse.isOk();

        step("Refresh the Deployment page to pick up the new data");
        browser.reload();
        user.is(Waiting.on(DeploymentPage.FIRMWARE_VERSIONS_TAB).within(15));

        step("Verify the Firmware Versions tab panel is present and contains firmware cards");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.PANEL)
                        .describedAs("Firmware Versions tab panel is visible after API-created family")
                        .isVisible()
        );

        step("Verify at least one firmware card is rendered in the tab panel");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.CARD)
                        .describedAs("At least one firmware card is visible — backend data reflected in UI")
                        .isVisible()
        );
    }

    // ── TC-35.9.03 ─────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-35.9.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_E2E")
    @Description("Upload Firmware form exposes FirmwareFamily association field")
    @Outcome("The Upload Firmware modal contains a 'Firmware Family' label and dropdown/select field "
            + "allowing the user to associate firmware with a FirmwareFamily")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_E2E}, priority = 3,
            enabled = false) // Enable after PS-35 front-end work ships the FirmwareFamily dropdown
    public void testUploadFirmwareFormExposesFirmwareFamilyField() {

        step("Click Upload Firmware button to open the modal");
        user.attemptsTo(DeploymentPageImpl.clickUploadFirmwareButton());

        step("Wait for the modal dialog to appear");
        user.is(Waiting.on(UploadModal.CONTAINER).within(10));

        step("Verify the 'Firmware Family' label is visible in the modal");
        user.wantsTo(
                Verify.uiElement(UploadModal.FIRMWARE_FAMILY_LABEL)
                        .describedAs("'Firmware Family' label is present in the Upload Firmware modal (PS-35)")
                        .isVisible()
        );

        step("Verify the Firmware Family dropdown/select field is present");
        user.wantsTo(
                Verify.uiElement(UploadModal.FIRMWARE_FAMILY_FIELD)
                        .describedAs("Firmware Family dropdown/select field is visible and interactable")
                        .isVisible()
        );

        step("Close the modal");
        user.attemptsTo(DeploymentPageImpl.clickCloseModal());
    }

    // ── TC-35.9.04 ─────────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-35.9.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_E2E")
    @Description("Authorization enforcement visible end-to-end for non-Admin")
    @Outcome("A non-Admin user attempting to create a FirmwareFamily via the API receives "
            + "an authorization error, and the UI does not show admin-only controls")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_E2E}, priority = 4,
            enabled = false) // Enable when non-Admin test credentials are available
    public void testAuthorizationEnforcementEndToEndForNonAdmin() {

        step("Attempt createFirmwareFamily API call with non-Admin credentials");
        // TODO: Use non-Admin auth credentials when available
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Non-Admin E2E\\", \
                targetModels: [\\"SG-AUTH\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify API rejects the mutation for non-Admin user");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("Unauthorized") || body.contains("Forbidden") || body.contains("error"),
                "Expected authorization error for non-Admin user, got: " + body
        );

        step("Verify the Deployment UI does not expose admin-only Upload Firmware button for non-Admin");
        // This verification would require logging in as a non-Admin user in the browser
        // which is handled by a separate @BeforeClass setup when non-Admin credentials are available
    }

    // ──────────────────────────── Helper Methods ────────────────────────────

    /**
     * Extracts a simple JSON string value by key from a flat JSON body.
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int valueStart = keyIndex + searchKey.length();
        int valueEnd = json.indexOf('"', valueStart);
        return valueEnd > valueStart ? json.substring(valueStart, valueEnd) : null;
    }
}
