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
import com.tpg.verification.Verify;
import static com.tpg.utils.CodeFillers.on;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.FIRMWARE_STATUS;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;

/**
 * Test suite for the Firmware Status badges on the Deployment page (Story PS-34 / AC-4).
 *
 * <p>Covers TC-34.4.05 — verifying that the Deployment page (/deployment) correctly renders
 * firmware status badges, that terminal-state cards hide irrelevant action buttons, and that
 * active-state cards show the correct action buttons.
 *
 * <p>The suite is split into two blocks:
 * <ol>
 *   <li><b>Current-state tests</b> (enabled) — validate the <em>pre-PS-34</em> UI behaviour
 *       against the live application. These serve as a regression baseline and confirm that the
 *       Deployment page, tab navigation, and existing badge colours work correctly.
 *   <li><b>Post-PS-34 tests</b> (enabled = false) — validate the <em>new 5-value enum</em>
 *       (Screening, Staged, Active, Deprecated, Recalled) once Story PS-34 has shipped to the
 *       target environment. Enable these tests by removing {@code enabled = false} and updating
 *       the test environment to the PS-34 build.
 * </ol>
 *
 * <p>All tests extend {@link InventoryTestBase}, which authenticates once per class in
 * {@code @BeforeClass}. Each test method re-navigates to the Deployment page via
 * {@code @BeforeMethod} to ensure a clean, isolated starting state.
 *
 * @see DeploymentPage
 * @see DeploymentPageImpl
 * @see InventoryTestBase
 * @jira PS-34 (Story: FirmwareFamily DynamoDB Entity &amp; AppSync Schema)
 * @jira PS-41 (QA Sub-task)
 */
public class FirmwareStatusTests extends InventoryTestBase {

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
    // BLOCK 1 — Current-state (pre-PS-34) baseline tests
    // These tests run against the live application as it exists today.
    // ══════════════════════════════════════════════════════════════════════════

    // ── TC-34.4.05-001 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-001",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that the Deployment page loads with the Firmware Versions tab active by default")
    @Outcome("Firmware Versions tab is visible and has aria-selected='true'; tab panel with firmware cards is rendered")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_STATUS})
    public void testDeploymentPageLoadsWithFirmwareVersionsTabActive() {

        step("Verify the Firmware Versions tab is visible");
        user.wantsTo(
                Verify.uiElement(DeploymentPage.FIRMWARE_VERSIONS_TAB)
                        .describedAs("button#tab-firmware is visible on the Deployment page")
                        .isVisible()
        );

        step("Verify the Audit Log tab is also visible (tab navigation is rendered)");
        user.wantsTo(
                Verify.uiElement(DeploymentPage.AUDIT_LOG_TAB)
                        .describedAs("button#tab-audit is visible — both tab buttons are rendered")
                        .isVisible()
        );

        step("Verify the Firmware Versions tab panel is present (aria-labelledby='tab-firmware')");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.PANEL)
                        .describedAs("[aria-labelledby='tab-firmware'] tabpanel is rendered")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-002 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-002",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that firmware cards are rendered in the Firmware Versions tab panel")
    @Outcome("At least one firmware card is visible; each card contains a status badge span")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_STATUS})
    public void testFirmwareCardsAndStatusBadgesAreRendered() {

        step("Verify at least one firmware card is present in the tab panel");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.CARD)
                        .describedAs("At least one .bg-card is rendered inside [aria-labelledby='tab-firmware']")
                        .isVisible()
        );

        step("Verify at least one status badge span is present inside a firmware card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.STATUS_BADGE)
                        .describedAs("span.inline-flex.rounded-full status badge is present on at least one firmware card")
                        .isVisible()
        );

        step("Verify at least one card heading (h3) is visible");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.CARD_HEADING)
                        .describedAs("h3.text-lg inside a firmware card is visible")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-003 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-003",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that 'Approved' firmware cards render a green status badge (bg-green-100)")
    @Outcome("At least one green-background (bg-green-100) status badge is visible inside a firmware card")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testApprovedFirmwareBadgeRendersWithGreenBackground() {

        step("Verify a firmware card with Approved (green) status badge is visible");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.APPROVED_BADGE)
                        .describedAs("span.bg-green-100 badge is visible — at least one Approved firmware card exists")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-004 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-004",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that 'Pending' firmware cards render an orange status badge (bg-orange-100)")
    @Outcome("At least one orange-background (bg-orange-100) status badge is visible inside a firmware card")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testPendingFirmwareBadgeRendersWithOrangeBackground() {

        step("Verify a firmware card with Pending (orange) status badge is visible");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.PENDING_BADGE)
                        .describedAs("span.bg-orange-100 badge is visible — at least one Pending firmware card exists")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-005 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-005",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that 'Deprecated' firmware cards render a red status badge (bg-red-100)")
    @Outcome("At least one red-background (bg-red-100) status badge is visible inside a firmware card")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testDeprecatedFirmwareBadgeRendersWithRedBackground() {

        step("Verify a firmware card with Deprecated (red) status badge is visible");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATED_BADGE)
                        .describedAs("span.bg-red-100 badge is visible — at least one Deprecated firmware card exists")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-006 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-006",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that Deprecated firmware cards show only the Details button (no Download or Deprecate)")
    @Outcome("Deprecated card does NOT show button.btn-primary (Download) or button.border-red-300 (Deprecate); Details toggle is visible")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testDeprecatedFirmwareCardShowsOnlyDetailsButton() {

        step("Verify a Deprecated firmware card is present (pre-condition)");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATED_BADGE)
                        .describedAs("Deprecated firmware card exists in the panel")
                        .isVisible()
        );

        step("Verify no Download button (btn-primary) is present on the page — Deprecated cards must not have it");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DOWNLOAD_BUTTON)
                        .describedAs("button.btn-primary is NOT visible — Deprecated cards do not show a Download button")
                        .isNotVisible()
        );

        step("Verify no Deprecate button (border-red-300) is present — Deprecated cards must not have it");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATE_BUTTON)
                        .describedAs("button.border-red-300 is NOT visible — Deprecated cards do not show a Deprecate button")
                        .isNotVisible()
        );

        step("Verify the Details expand toggle is visible on at least one card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DETAILS_TOGGLE)
                        .describedAs("button[aria-expanded] Details toggle is visible on at least one firmware card")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-007 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-007",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify that Approved firmware cards expose the Download and Deprecate action buttons")
    @Outcome("button.btn-primary (Download) and button.border-red-300 (Deprecate) are visible in the Approved firmware card")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testApprovedFirmwareCardShowsDownloadAndDeprecateButtons() {

        step("Verify an Approved firmware card is present (pre-condition)");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.APPROVED_BADGE)
                        .describedAs("Approved firmware card exists in the panel")
                        .isVisible()
        );

        step("Verify Download button (button.btn-primary) is visible on an Approved firmware card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DOWNLOAD_BUTTON)
                        .describedAs("button.btn-primary (Download) is visible — Approved firmware card shows the Download action")
                        .isVisible()
        );

        step("Verify Deprecate button (button.border-red-300) is visible on an Approved firmware card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATE_BUTTON)
                        .describedAs("button.border-red-300 (Deprecate) is visible — Approved firmware card shows the Deprecate action")
                        .isVisible()
        );
    }

    // ── TC-34.4.05-008 ──────────────────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-008",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("Verify switching to the Audit Log tab updates the page content correctly")
    @Outcome("Audit Log tab becomes aria-selected='true'; Export Full Log button appears; firmware cards are no longer visible")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS})
    public void testAuditLogTabSwitchesContentPanel() {

        step("Click the Audit Log tab to switch the content panel");
        user.attemptsTo(DeploymentPageImpl.clickAuditLogTab());

        step("Wait for the Audit Log tab to become active");
        user.is(Waiting.on(DeploymentPage.AuditLog.EXPORT_BUTTON).within(10));

        step("Verify Export Full Log button is visible — Audit Log panel is rendered");
        user.wantsTo(
                Verify.uiElement(DeploymentPage.AuditLog.EXPORT_BUTTON)
                        .describedAs("'Export Full Log' button is visible inside the Audit Log tab panel")
                        .isVisible()
        );

        step("Verify firmware card panels are no longer visible after switching to Audit Log tab");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.PANEL)
                        .describedAs("[aria-labelledby='tab-firmware'] panel is hidden / not visible when Audit Log tab is active")
                        .isNotVisible()
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // BLOCK 2 — Post-PS-34 tests (enabled = false until PS-34 ships)
    // Enable by removing `enabled = false` once the PS-34 backend and front-end
    // are deployed to the target environment.
    // ══════════════════════════════════════════════════════════════════════════

    // ── TC-34.4.05-009 (post-PS-34) ─────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-009",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("[POST-PS-34] Verify firmware cards show only the five new enum status values after PS-34 ships")
    @Outcome("Status badges display only: Screening, Staged, Active, Deprecated, or Recalled. " +
            "No card shows Pending, Approved, or Rejected.")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS}, enabled = false)
    public void testPostPs34OnlyNewEnumStatusBadgesAreVisible() {

        step("[POST-PS-34] Verify 'Screening' badge is present — new enum value replaces Pending");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_SCREENING_BADGE)
                        .describedAs("span:has-text('Screening') is visible — Screening enum value renders correctly")
                        .isVisible()
        );

        step("[POST-PS-34] Verify 'Active' badge is present — replaces Approved");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_ACTIVE_BADGE)
                        .describedAs("span:has-text('Active') is visible — Active enum value renders correctly")
                        .isVisible()
        );

        step("[POST-PS-34] Verify legacy 'Pending' badge is NOT visible after PS-34 deployment");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_ABSENT_PENDING_BADGE)
                        .describedAs("span:has-text('Pending') is NOT visible — legacy value removed by PS-34")
                        .isNotVisible()
        );

        step("[POST-PS-34] Verify legacy 'Approved' badge is NOT visible after PS-34 deployment");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_ABSENT_APPROVED_BADGE)
                        .describedAs("span:has-text('Approved') is NOT visible — legacy value removed by PS-34")
                        .isNotVisible()
        );

        step("[POST-PS-34] Verify legacy 'Rejected' badge is NOT visible after PS-34 deployment");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_ABSENT_REJECTED_BADGE)
                        .describedAs("span:has-text('Rejected') is NOT visible — legacy value removed by PS-34")
                        .isNotVisible()
        );
    }

    // ── TC-34.4.05-010 (post-PS-34) ─────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-010",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("[POST-PS-34] Verify 'Recalled' terminal-state cards show only the Details button")
    @Outcome("A Recalled firmware card exists; it does NOT show Download or Deprecate buttons")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS}, enabled = false)
    public void testPostPs34RecalledFirmwareCardShowsOnlyDetailsButton() {

        step("[POST-PS-34] Verify a Recalled firmware card badge is present");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_RECALLED_BADGE)
                        .describedAs("span:has-text('Recalled') is visible — Recalled firmware card exists")
                        .isVisible()
        );

        step("[POST-PS-34] Verify Download button is NOT present — Recalled is a terminal state");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DOWNLOAD_BUTTON)
                        .describedAs("button.btn-primary is NOT visible — Recalled cards must not expose Download action")
                        .isNotVisible()
        );

        step("[POST-PS-34] Verify Deprecate button is NOT present — Recalled is a terminal state");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATE_BUTTON)
                        .describedAs("button.border-red-300 is NOT visible — Recalled cards must not expose Deprecate action")
                        .isNotVisible()
        );
    }

    // ── TC-34.4.05-011 (post-PS-34) ─────────────────────────────────────────

    @MetaData(author = "QA Automation", testCaseId = "TC-34.4.05-011",
            stories = {"PS-34", "PS-41"}, category = "FIRMWARE_STATUS")
    @Description("[POST-PS-34] Verify 'Active' firmware cards expose Download and Deprecate buttons (replaces Approved)")
    @Outcome("Active firmware card shows button.btn-primary (Download) and button.border-red-300 (Deprecate)")
    @Test(groups = {REGRESSION, FIRMWARE_STATUS}, enabled = false)
    public void testPostPs34ActiveFirmwareCardShowsDownloadAndDeprecateButtons() {

        step("[POST-PS-34] Verify an Active firmware card is present");
        user.wantsTo(
                Verify.uiElement(FirmwareCard._PS34_ACTIVE_BADGE)
                        .describedAs("Active badge is visible — Active firmware card exists")
                        .isVisible()
        );

        step("[POST-PS-34] Verify Download button is visible on the Active firmware card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DOWNLOAD_BUTTON)
                        .describedAs("button.btn-primary (Download) is visible on Active firmware card")
                        .isVisible()
        );

        step("[POST-PS-34] Verify Deprecate button is visible on the Active firmware card");
        user.wantsTo(
                Verify.uiElement(FirmwareCard.DEPRECATE_BUTTON)
                        .describedAs("button.border-red-300 (Deprecate) is visible on Active firmware card")
                        .isVisible()
        );
    }
}
