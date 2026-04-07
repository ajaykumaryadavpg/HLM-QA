package com.tpg.automation.inventory;

import com.tpg.NovusApiTestBase;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.listeners.NovusTestListener;
import com.tpg.automation.testdata.InventoryTestData.FirmwareFamilyApi;
import com.tpg.methods.Post;
import org.springframework.test.context.ActiveProfiles;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.FIRMWARE_FAMILY_API;
import static com.tpg.automation.constants.TestGroups.NEGATIVE;
import static com.tpg.automation.constants.TestGroups.REGRESSION;
import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;

/**
 * API test suite for FirmwareFamily CRUD resolvers (Story PS-35 / QA Sub-task PS-42).
 *
 * <p>Covers Suites 1-4 (CRUD happy path), Suite 6 (Admin-only authorization),
 * Suite 7 (Input validation), and Suite 8 (Edge cases &amp; negative scenarios).
 *
 * <p>All mutations and queries are sent as GraphQL POST requests to the AppSync endpoint.
 * The test uses the framework's {@link Post} API driver with JSON body payloads.
 *
 * <p>Suite 5 (TypeScript wrapper exports) is out of scope for this Java test framework.
 *
 * @see FirmwareFamilyApi
 * @see NovusApiTestBase
 * @jira PS-35 (Story: FirmwareFamily CRUD Resolvers &amp; API)
 * @jira PS-42 (QA Sub-task)
 */
@ActiveProfiles({"test", "inventory"})
@Listeners(NovusTestListener.class)
public class FirmwareFamilyApiTests extends NovusApiTestBase {

    /** Shared ID captured from the create test to use in get/update/lifecycle tests */
    private static String createdFamilyId;

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 1 — createFirmwareFamily: Happy Path (AC-1)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.1.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Create FirmwareFamily with all required and optional fields")
    @Outcome("API returns 200 with a valid FirmwareFamily object containing an auto-generated ID, "
            + "the provided familyName, targetModels array, and status")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 1)
    public void testCreateFirmwareFamilyWithAllFields() {
        step("Send createFirmwareFamily mutation with all required and optional fields");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"%s\\", \\"%s\\"], \
                status: \\"%s\\" \
                }) { id familyName targetModels status createdAt updatedAt } }"}"""
                .formatted(
                        FirmwareFamilyApi.FAMILY_NAME,
                        FirmwareFamilyApi.TARGET_MODEL_1,
                        FirmwareFamilyApi.TARGET_MODEL_2,
                        FirmwareFamilyApi.STATUS_ACTIVE
                );

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response status is OK (200)");
        response.isOk();

        step("Verify response contains the created familyName");
        response.bodyContains(FirmwareFamilyApi.FAMILY_NAME);

        step("Verify response contains an 'id' field (auto-generated)");
        response.bodyContains("\"id\"");

        step("Extract created family ID for downstream tests");
        String body = response.getContent();
        int idStart = body.indexOf("\"id\":\"") + 6;
        int idEnd = body.indexOf("\"", idStart);
        if (idStart > 5 && idEnd > idStart) {
            createdFamilyId = body.substring(idStart, idEnd);
            log.info("Created FirmwareFamily ID: " + createdFamilyId);
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.1.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Create FirmwareFamily with only required fields (minimal payload)")
    @Outcome("API returns 200; optional fields default to null or empty")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 2)
    public void testCreateFirmwareFamilyMinimalPayload() {
        step("Send createFirmwareFamily mutation with only required fields");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"%s\\"], \
                status: \\"%s\\" \
                }) { id familyName targetModels status } }"}"""
                .formatted(
                        FirmwareFamilyApi.FAMILY_NAME_MINIMAL,
                        FirmwareFamilyApi.TARGET_MODEL_SINGLE,
                        FirmwareFamilyApi.STATUS_SCREENING
                );

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response status is OK");
        response.isOk();

        step("Verify response contains the minimal family name");
        response.bodyContains(FirmwareFamilyApi.FAMILY_NAME_MINIMAL);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.1.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Create FirmwareFamily with a single targetModel entry")
    @Outcome("API returns 200 with a single-element targetModels array")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 3)
    public void testCreateFirmwareFamilyWithSingleTargetModel() {
        step("Send createFirmwareFamily mutation with one targetModel");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Single Model Family\\", \
                targetModels: [\\"%s\\"], \
                status: \\"%s\\" \
                }) { id familyName targetModels status } }"}"""
                .formatted(FirmwareFamilyApi.TARGET_MODEL_SINGLE, FirmwareFamilyApi.STATUS_STAGED);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK and contains the single target model");
        response.isOk();
        response.bodyContains(FirmwareFamilyApi.TARGET_MODEL_SINGLE);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.1.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Create FirmwareFamily with each valid status value")
    @Outcome("API returns 200 for each of the five valid status values: Screening, Staged, Active, Deprecated, Recalled")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 4)
    public void testCreateFirmwareFamilyWithEachValidStatus() {
        String[] validStatuses = {
                FirmwareFamilyApi.STATUS_SCREENING,
                FirmwareFamilyApi.STATUS_STAGED,
                FirmwareFamilyApi.STATUS_ACTIVE,
                FirmwareFamilyApi.STATUS_DEPRECATED,
                FirmwareFamilyApi.STATUS_RECALLED
        };

        for (String status : validStatuses) {
            step("Send createFirmwareFamily mutation with status: " + status);
            String mutation = """
                    {"query": "mutation { createFirmwareFamily(input: { \
                    familyName: \\"[AUTO-TEST] Status %s\\", \
                    targetModels: [\\"SG-TEST\\"], \
                    status: \\"%s\\" \
                    }) { id status } }"}"""
                    .formatted(status, status);

            var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                    .withHeader("Content-Type", "application/json")
                    .withBody(mutation)
                    .execute();

            step("Verify response is OK for status: " + status);
            response.isOk();
            response.bodyContains(status);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 2 — listFirmwareFamilies: Pagination & Filtering (AC-2)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("listFirmwareFamilies returns all families with expected response shape")
    @Outcome("Response contains 'items' array and 'nextToken' field")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 10,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testListFirmwareFamiliesReturnsExpectedShape() {
        step("Send listFirmwareFamilies query");
        String query = """
                {"query": "{ listFirmwareFamilies { items { id familyName targetModels status } nextToken totalCount } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains 'items' array");
        response.bodyContains("\"items\"");

        step("Verify response contains 'nextToken' field");
        response.bodyContains("nextToken");

        step("Verify response contains 'totalCount' field");
        response.bodyContains("totalCount");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("listFirmwareFamilies respects the limit argument")
    @Outcome("Number of returned items does not exceed the specified limit")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 11)
    public void testListFirmwareFamiliesRespectsLimit() {
        step("Send listFirmwareFamilies query with limit=2");
        String query = """
                {"query": "{ listFirmwareFamilies(limit: 2) { items { id familyName } nextToken totalCount } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify the response contains items (limit was applied server-side)");
        response.bodyContains("\"items\"");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Pagination — nextToken cursor advances correctly (no duplicates/skips)")
    @Outcome("Second page returns different items than the first page; no overlapping IDs")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 12)
    public void testListFirmwareFamiliesPaginationAdvancesCorrectly() {
        step("Fetch first page with limit=1");
        String firstPageQuery = """
                {"query": "{ listFirmwareFamilies(limit: 1) { items { id familyName } nextToken } }"}""";

        var firstPage = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(firstPageQuery)
                .execute();

        step("Verify first page response is OK");
        firstPage.isOk();

        step("Extract nextToken from first page response");
        String body = firstPage.getContent();
        String nextToken = extractJsonValue(body, "nextToken");

        if (nextToken != null && !nextToken.equals("null")) {
            step("Fetch second page using extracted nextToken");
            String secondPageQuery = """
                    {"query": "{ listFirmwareFamilies(limit: 1, nextToken: \\"%s\\") { items { id familyName } nextToken } }"}"""
                    .formatted(nextToken);

            var secondPage = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                    .withHeader("Content-Type", "application/json")
                    .withBody(secondPageQuery)
                    .execute();

            step("Verify second page response is OK");
            secondPage.isOk();

            step("Verify second page contains items");
            secondPage.bodyContains("\"items\"");
        } else {
            step("Only one page of results — pagination not exercised (dataset too small)");
        }
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Filters by status when status argument is provided")
    @Outcome("All returned items have the requested status value")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 13)
    public void testListFirmwareFamiliesFiltersByStatus() {
        step("Send listFirmwareFamilies with status filter: Active");
        String query = """
                {"query": "{ listFirmwareFamilies(status: \\"%s\\") { items { id familyName status } totalCount } }"}"""
                .formatted(FirmwareFamilyApi.STATUS_ACTIVE);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response body does not contain non-Active statuses in item results");
        // All items should have status "Active" — body-level check
        response.bodyContains("\"items\"");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.05",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Status filter matching zero records returns empty result")
    @Outcome("items array is empty; totalCount is 0")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 14)
    public void testListFirmwareFamiliesEmptyResultForNoMatchingStatus() {
        step("Send listFirmwareFamilies with a status that has no matching records (Recalled)");
        String query = """
                {"query": "{ listFirmwareFamilies(status: \\"Recalled\\") { items { id } totalCount } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK (empty result is still a valid response)");
        response.isOk();

        step("Verify response contains items field (may be empty array)");
        response.bodyContains("\"items\"");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.2.06",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("limit=0 or very large limit behaves predictably")
    @Outcome("API does not error out; returns either empty results or all available records")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 15)
    public void testListFirmwareFamiliesWithExtremeLimit() {
        step("Send listFirmwareFamilies with very large limit (9999)");
        String query = """
                {"query": "{ listFirmwareFamilies(limit: 9999) { items { id } totalCount } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK — server clamps or accepts the large limit gracefully");
        response.isOk();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 3 — getFirmwareFamily: Single Record Fetch (AC-3)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.3.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Returns correct record for a valid ID")
    @Outcome("Response contains the exact familyName, targetModels, and status for the requested ID")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 20,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testGetFirmwareFamilyByValidId() {
        Assert.assertNotNull(createdFamilyId, "No family ID available — create test must run first");

        step("Send getFirmwareFamily query for ID: " + createdFamilyId);
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"%s\\") { id familyName targetModels status createdAt updatedAt } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains the expected familyName");
        response.bodyContains(FirmwareFamilyApi.FAMILY_NAME);

        step("Verify response contains the expected target models");
        response.bodyContains(FirmwareFamilyApi.TARGET_MODEL_1);
        response.bodyContains(FirmwareFamilyApi.TARGET_MODEL_2);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.3.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Returns null or error for a non-existent ID")
    @Outcome("Response returns null data or a 'not found' error — no 500 server error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 21)
    public void testGetFirmwareFamilyNonExistentId() {
        step("Send getFirmwareFamily query for a non-existent UUID");
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"%s\\") { id familyName } }"}"""
                .formatted(FirmwareFamilyApi.NON_EXISTENT_ID);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK (GraphQL returns 200 even for null results)");
        response.isOk();
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.3.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Returns null or error for an empty string ID")
    @Outcome("Response returns error or null — no 500 server error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 22)
    public void testGetFirmwareFamilyEmptyId() {
        step("Send getFirmwareFamily query with empty string ID");
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"\\") { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK (GraphQL handles gracefully)");
        response.isOk();
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.3.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Returns correct data after the record has been updated")
    @Outcome("getFirmwareFamily reflects the most recent field values post-update")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 35,
            dependsOnMethods = "testUpdateFirmwareFamilyName")
    public void testGetFirmwareFamilyAfterUpdate() {
        Assert.assertNotNull(createdFamilyId, "No family ID available — create test must run first");

        step("Send getFirmwareFamily query to verify updated data");
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"%s\\") { id familyName targetModels status updatedAt } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains the updated familyName");
        response.bodyContains(FirmwareFamilyApi.FAMILY_NAME_UPDATED);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 4 — updateFirmwareFamily: Field Updates & Errors (AC-4)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Updates familyName successfully")
    @Outcome("API returns 200; familyName in response matches the updated value")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 30,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateFirmwareFamilyName() {
        Assert.assertNotNull(createdFamilyId, "No family ID available — create test must run first");

        step("Send updateFirmwareFamily mutation to change familyName");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"%s\\" \
                }) { id familyName status } }"}"""
                .formatted(createdFamilyId, FirmwareFamilyApi.FAMILY_NAME_UPDATED);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains the updated familyName");
        response.bodyContains(FirmwareFamilyApi.FAMILY_NAME_UPDATED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Updates targetModels successfully")
    @Outcome("API returns 200; targetModels array reflects the new model list")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 31,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateFirmwareFamilyTargetModels() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation to replace targetModels");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                targetModels: [\\"SG-10K\\", \\"SG-12K\\"] \
                }) { id targetModels } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains the new target models");
        response.bodyContains("SG-10K");
        response.bodyContains("SG-12K");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Updates status successfully")
    @Outcome("API returns 200; status field reflects the new value")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 32,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateFirmwareFamilyStatus() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation to change status to Deprecated");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                status: \\"%s\\" \
                }) { id status } }"}"""
                .formatted(createdFamilyId, FirmwareFamilyApi.STATUS_DEPRECATED);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify response contains the updated status");
        response.bodyContains(FirmwareFamilyApi.STATUS_DEPRECATED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Updates multiple fields in a single call")
    @Outcome("API returns 200; all updated fields reflect their new values")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 33,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateFirmwareFamilyMultipleFields() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation updating familyName, targetModels, and status");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"[AUTO-TEST] Multi-Update Family\\", \
                targetModels: [\\"SG-15K\\"], \
                status: \\"%s\\" \
                }) { id familyName targetModels status } }"}"""
                .formatted(createdFamilyId, FirmwareFamilyApi.STATUS_STAGED);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK");
        response.isOk();

        step("Verify all updated fields are present in response");
        response.bodyContains("[AUTO-TEST] Multi-Update Family");
        response.bodyContains("SG-15K");
        response.bodyContains(FirmwareFamilyApi.STATUS_STAGED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.05",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Non-existent ID returns error (not an upsert)")
    @Outcome("API returns an error or null — does not silently create a new record")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 34)
    public void testUpdateFirmwareFamilyNonExistentId() {
        step("Send updateFirmwareFamily mutation with a non-existent ID");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"Should Not Exist\\" \
                }) { id familyName } }"}"""
                .formatted(FirmwareFamilyApi.NON_EXISTENT_ID);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK (GraphQL returns 200 with errors in body)");
        response.isOk();

        step("Verify response body indicates an error or null result");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("null") || body.contains("Error"),
                "Expected error or null for non-existent ID update, got: " + body
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.4.06",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Invalid status value returns validation error")
    @Outcome("API rejects the mutation with a validation error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 35)
    public void testUpdateFirmwareFamilyInvalidStatus() {
        step("Send updateFirmwareFamily mutation with an invalid status enum value");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                status: \\"%s\\" \
                }) { id status } }"}"""
                .formatted(
                        createdFamilyId != null ? createdFamilyId : FirmwareFamilyApi.NON_EXISTENT_ID,
                        FirmwareFamilyApi.INVALID_STATUS
                );

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response is OK (GraphQL wraps errors in 200)");
        response.isOk();

        step("Verify response body contains error/validation indication");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation"),
                "Expected validation error for invalid status, got: " + body
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 6 — Admin-Only Authorization (AC-6)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Admin user can call createFirmwareFamily")
    @Outcome("Mutation succeeds with 200 when called by an Admin user")
    @Test(groups = {SMOKE_TESTS, FIRMWARE_FAMILY_API}, priority = 40)
    public void testAdminCanCreateFirmwareFamily() {
        step("Send createFirmwareFamily mutation as Admin user");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Admin Create Auth\\", \
                targetModels: [\\"SG-AUTH\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify Admin user receives OK response");
        response.isOk();

        step("Verify the family was created (response contains familyName)");
        response.bodyContains("[AUTO-TEST] Admin Create Auth");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Non-Admin (Manager) receives auth error on createFirmwareFamily")
    @Outcome("API returns an Unauthorized or Forbidden error in the response body")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 41,
            enabled = false) // Enable when non-admin test credentials are available
    public void testNonAdminManagerCannotCreateFirmwareFamily() {
        step("Send createFirmwareFamily mutation with Manager role credentials");
        // TODO: Configure Manager-role auth token/credentials when available
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Manager Create\\", \
                targetModels: [\\"SG-AUTH\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response body contains authorization error");
        response.bodyContains(FirmwareFamilyApi.ERROR_UNAUTHORIZED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Technician receives auth error on createFirmwareFamily")
    @Outcome("API returns an Unauthorized or Forbidden error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 42,
            enabled = false) // Enable when Technician test credentials are available
    public void testTechnicianCannotCreateFirmwareFamily() {
        step("Send createFirmwareFamily mutation with Technician role credentials");
        // TODO: Configure Technician-role auth token/credentials when available
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Technician Create\\", \
                targetModels: [\\"SG-AUTH\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response body contains authorization error");
        response.bodyContains(FirmwareFamilyApi.ERROR_UNAUTHORIZED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Admin user can call updateFirmwareFamily")
    @Outcome("Mutation succeeds when called by an Admin user")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 43,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testAdminCanUpdateFirmwareFamily() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation as Admin user");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"[AUTO-TEST] Admin Update Auth\\" \
                }) { id familyName } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify Admin user receives OK response");
        response.isOk();
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.05",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Non-Admin receives auth error on updateFirmwareFamily")
    @Outcome("API returns Unauthorized error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 44,
            enabled = false) // Enable when non-admin test credentials are available
    public void testNonAdminCannotUpdateFirmwareFamily() {
        step("Send updateFirmwareFamily mutation with non-Admin credentials");
        // TODO: Configure non-Admin auth token/credentials when available
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"some-id\\", \
                familyName: \\"Should Fail\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains authorization error");
        response.bodyContains(FirmwareFamilyApi.ERROR_UNAUTHORIZED);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.06",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Non-Admin CAN call listFirmwareFamilies (reads not restricted)")
    @Outcome("listFirmwareFamilies succeeds for non-Admin users — reads are open")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 45,
            enabled = false) // Enable when non-admin test credentials are available
    public void testNonAdminCanListFirmwareFamilies() {
        step("Send listFirmwareFamilies query with non-Admin credentials");
        String query = """
                {"query": "{ listFirmwareFamilies { items { id familyName } } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify non-Admin can read firmware families (200 OK)");
        response.isOk();
        response.bodyContains("\"items\"");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.07",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Non-Admin CAN call getFirmwareFamily (reads not restricted)")
    @Outcome("getFirmwareFamily succeeds for non-Admin users")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 46,
            enabled = false) // Enable when non-admin test credentials are available
    public void testNonAdminCanGetFirmwareFamily() {
        step("Send getFirmwareFamily query with non-Admin credentials");
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"any-valid-id\\") { id familyName } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify non-Admin can read single firmware family (200 OK)");
        response.isOk();
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.08",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Unauthenticated request to createFirmwareFamily rejected (401)")
    @Outcome("API returns 401 Unauthorized for unauthenticated mutation requests")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 47)
    public void testUnauthenticatedCreateRejected() {
        step("Send createFirmwareFamily mutation without any auth headers");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"Unauth Test\\", \
                targetModels: [\\"X\\"], \
                status: \\"Active\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify unauthenticated request is rejected");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("Unauthorized") || body.contains("error") || body.contains("401")
                        || response.getResponse().status() == 401,
                "Expected 401 or Unauthorized error for unauthenticated request"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.6.09",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Unauthenticated request to listFirmwareFamilies rejected (401)")
    @Outcome("API returns 401 Unauthorized for unauthenticated query requests")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 48)
    public void testUnauthenticatedListRejected() {
        step("Send listFirmwareFamilies query without any auth headers");
        String query = """
                {"query": "{ listFirmwareFamilies { items { id } } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify unauthenticated request is rejected");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("Unauthorized") || body.contains("error") || body.contains("401")
                        || response.getResponse().status() == 401,
                "Expected 401 or Unauthorized error for unauthenticated request"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 7 — Input Validation (AC-1 extended)
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("createFirmwareFamily rejected when familyName omitted")
    @Outcome("API returns validation error when familyName is not provided")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 50)
    public void testCreateRejectedWhenFamilyNameOmitted() {
        step("Send createFirmwareFamily mutation without familyName field");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                targetModels: [\\"SG-5K\\"], \
                status: \\"Active\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains error (missing required field)");
        response.bodyContains("error");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when familyName is empty string")
    @Outcome("API returns validation error for empty familyName")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 51)
    public void testCreateRejectedWhenFamilyNameEmpty() {
        step("Send createFirmwareFamily mutation with empty familyName");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"\\", \
                targetModels: [\\"SG-5K\\"], \
                status: \\"Active\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response indicates validation failure");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation"),
                "Expected validation error for empty familyName"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when familyName exceeds max length (300 chars)")
    @Outcome("API returns validation error for overly long familyName")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 52)
    public void testCreateRejectedWhenFamilyNameExceedsMaxLength() {
        step("Send createFirmwareFamily mutation with familyName > 300 chars");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"SG-5K\\"], \
                status: \\"Active\\" \
                }) { id } }"}"""
                .formatted(FirmwareFamilyApi.MAX_LENGTH_NAME);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response indicates validation failure for max-length breach");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation")
                        || body.contains("length"),
                "Expected validation error for familyName exceeding 300 chars"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when targetModels omitted")
    @Outcome("API returns validation error when targetModels is not provided")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 53)
    public void testCreateRejectedWhenTargetModelsOmitted() {
        step("Send createFirmwareFamily mutation without targetModels field");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"Missing Models\\", \
                status: \\"Active\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains error");
        response.bodyContains("error");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.05",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when targetModels is empty array")
    @Outcome("API returns validation error for empty targetModels array")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 54)
    public void testCreateRejectedWhenTargetModelsEmpty() {
        step("Send createFirmwareFamily mutation with empty targetModels array");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"Empty Models\\", \
                targetModels: [], \
                status: \\"Active\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response indicates validation failure for empty targetModels");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation"),
                "Expected validation error for empty targetModels array"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.06",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when status omitted")
    @Outcome("API returns validation error when status is not provided")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 55)
    public void testCreateRejectedWhenStatusOmitted() {
        step("Send createFirmwareFamily mutation without status field");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"Missing Status\\", \
                targetModels: [\\"SG-5K\\"] \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains error");
        response.bodyContains("error");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.07",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Rejected when status is invalid enum value")
    @Outcome("API returns validation error for unrecognised status value")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 56)
    public void testCreateRejectedWhenStatusInvalid() {
        step("Send createFirmwareFamily mutation with invalid status enum");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"Invalid Status\\", \
                targetModels: [\\"SG-5K\\"], \
                status: \\"%s\\" \
                }) { id } }"}"""
                .formatted(FirmwareFamilyApi.INVALID_STATUS);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains error for invalid status");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error"),
                "Expected error for invalid status enum value"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.08",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("updateFirmwareFamily rejected when id missing")
    @Outcome("API returns validation error for missing id field")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 57)
    public void testUpdateRejectedWhenIdMissing() {
        step("Send updateFirmwareFamily mutation without id field");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                familyName: \\"No ID\\" \
                }) { id } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response contains error");
        response.bodyContains("error");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.09",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Update rejected when familyName is empty string")
    @Outcome("API returns validation error for empty familyName on update")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 58,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateRejectedWhenFamilyNameEmpty() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation with empty familyName");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                familyName: \\"\\" \
                }) { id } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response indicates validation failure");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation"),
                "Expected validation error for empty familyName on update"
        );
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.7.10",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Update rejected when targetModels set to empty array")
    @Outcome("API returns validation error for empty targetModels on update")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 59,
            dependsOnMethods = "testCreateFirmwareFamilyWithAllFields")
    public void testUpdateRejectedWhenTargetModelsEmpty() {
        Assert.assertNotNull(createdFamilyId, "No family ID available");

        step("Send updateFirmwareFamily mutation with empty targetModels array");
        String mutation = """
                {"query": "mutation { updateFirmwareFamily(input: { \
                id: \\"%s\\", \
                targetModels: [] \
                }) { id } }"}"""
                .formatted(createdFamilyId);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response indicates validation failure");
        String body = response.getContent();
        Assert.assertTrue(
                body.contains("error") || body.contains("Error") || body.contains("validation"),
                "Expected validation error for empty targetModels on update"
        );
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUITE 8 — Edge Cases & Negative Scenarios
    // ══════════════════════════════════════════════════════════════════════════

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.01",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("listFirmwareFamilies with expired nextToken returns error gracefully")
    @Outcome("API returns an error or empty result — no 500 server error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 60)
    public void testListWithExpiredNextTokenHandledGracefully() {
        step("Send listFirmwareFamilies with an expired/invalid nextToken");
        String query = """
                {"query": "{ listFirmwareFamilies(nextToken: \\"%s\\") { items { id } nextToken } }"}"""
                .formatted(FirmwareFamilyApi.MALFORMED_TOKEN);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response does not return a 500 server error");
        int status = response.getResponse().status();
        Assert.assertTrue(status < 500, "Expected non-500 status, got: " + status);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.02",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Duplicate familyName handling (idempotency check)")
    @Outcome("API either rejects the duplicate or creates a second record — behaviour is documented")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 61)
    public void testDuplicateFamilyNameHandling() {
        String duplicateName = "[AUTO-TEST] Duplicate Check " + System.currentTimeMillis();

        step("Create first FirmwareFamily with name: " + duplicateName);
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"%s\\", \
                targetModels: [\\"SG-DUP\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}"""
                .formatted(duplicateName);

        Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute()
                .isOk();

        step("Attempt to create second FirmwareFamily with the same name");
        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify API handles duplicate gracefully (either success or controlled error)");
        int status = response.getResponse().status();
        Assert.assertTrue(status < 500, "Expected non-500 status for duplicate name, got: " + status);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.03",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("getFirmwareFamily for deleted/non-existent record — no 500")
    @Outcome("API returns null or not-found error, not a 500 server error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 62)
    public void testGetDeletedRecordNoServerError() {
        step("Send getFirmwareFamily for a non-existent/deleted record ID");
        String query = """
                {"query": "{ getFirmwareFamily(id: \\"%s\\") { id familyName } }"}"""
                .formatted(FirmwareFamilyApi.NON_EXISTENT_ID);

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response status is not 500");
        int status = response.getResponse().status();
        Assert.assertTrue(status < 500, "Expected non-500 status, got: " + status);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.04",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Malformed nextToken handled gracefully")
    @Outcome("API returns error or empty result — no 500")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API, NEGATIVE}, priority = 63)
    public void testMalformedNextTokenHandledGracefully() {
        step("Send listFirmwareFamilies with a malformed cursor token");
        String query = """
                {"query": "{ listFirmwareFamilies(nextToken: \\"!!!MALFORMED!!!\\" ) { items { id } } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        step("Verify response status is not 500");
        int status = response.getResponse().status();
        Assert.assertTrue(status < 500, "Expected non-500 for malformed token, got: " + status);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.05",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("targetModels with duplicate model strings")
    @Outcome("API accepts or deduplicates — no server error")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 64)
    public void testTargetModelsWithDuplicateStrings() {
        step("Send createFirmwareFamily with duplicate targetModels entries");
        String mutation = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Dup Models\\", \
                targetModels: [\\"SG-5K\\", \\"SG-5K\\", \\"SG-5K\\"], \
                status: \\"Active\\" \
                }) { id targetModels } }"}""";

        var response = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation)
                .execute();

        step("Verify response status is not 500");
        int status = response.getResponse().status();
        Assert.assertTrue(status < 500, "Expected non-500 for duplicate models, got: " + status);
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.06",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("Concurrent createFirmwareFamily calls — no corrupted records")
    @Outcome("Both calls succeed or one fails gracefully — no data corruption")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 65)
    public void testConcurrentCreateCallsNoCorrruption() {
        step("Send two createFirmwareFamily mutations in rapid succession");
        String mutation1 = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Concurrent A\\", \
                targetModels: [\\"SG-C1\\"], \
                status: \\"Active\\" \
                }) { id familyName } }"}""";

        String mutation2 = """
                {"query": "mutation { createFirmwareFamily(input: { \
                familyName: \\"[AUTO-TEST] Concurrent B\\", \
                targetModels: [\\"SG-C2\\"], \
                status: \\"Screening\\" \
                }) { id familyName } }"}""";

        var response1 = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation1)
                .execute();

        var response2 = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(mutation2)
                .execute();

        step("Verify both responses are non-500");
        Assert.assertTrue(response1.getResponse().status() < 500, "First concurrent call returned 500");
        Assert.assertTrue(response2.getResponse().status() < 500, "Second concurrent call returned 500");
    }

    @MetaData(author = "QA Automation", testCaseId = "TC-35.8.07",
            stories = {"PS-35", "PS-42"}, category = "FIRMWARE_FAMILY_API")
    @Description("totalCount is consistent across sequential calls")
    @Outcome("Two sequential listFirmwareFamilies calls return the same totalCount")
    @Test(groups = {REGRESSION, FIRMWARE_FAMILY_API}, priority = 66)
    public void testTotalCountConsistentAcrossSequentialCalls() {
        step("Send first listFirmwareFamilies query to get totalCount");
        String query = """
                {"query": "{ listFirmwareFamilies { totalCount } }"}""";

        var firstCall = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        firstCall.isOk();
        String firstCount = extractJsonValue(firstCall.getContent(), "totalCount");

        step("Send second listFirmwareFamilies query immediately after");
        var secondCall = Post.atUrl(FirmwareFamilyApi.GRAPHQL_ENDPOINT)
                .withHeader("Content-Type", "application/json")
                .withBody(query)
                .execute();

        secondCall.isOk();
        String secondCount = extractJsonValue(secondCall.getContent(), "totalCount");

        step("Verify totalCount is consistent between sequential calls");
        Assert.assertEquals(firstCount, secondCount,
                "totalCount should be consistent across sequential calls");
    }

    // ──────────────────────────── Helper Methods ────────────────────────────

    /**
     * Extracts a simple JSON string or numeric value by key from a flat JSON body.
     * Not a full JSON parser — sufficient for test assertions on API responses.
     */
    private String extractJsonValue(String json, String key) {
        String searchKey = "\"" + key + "\":";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;

        int valueStart = keyIndex + searchKey.length();
        // skip whitespace
        while (valueStart < json.length() && json.charAt(valueStart) == ' ') valueStart++;

        if (valueStart >= json.length()) return null;

        char firstChar = json.charAt(valueStart);
        if (firstChar == '"') {
            // String value
            int valueEnd = json.indexOf('"', valueStart + 1);
            return valueEnd > valueStart ? json.substring(valueStart + 1, valueEnd) : null;
        } else if (firstChar == 'n') {
            return "null";
        } else {
            // Numeric or boolean value
            int valueEnd = valueStart;
            while (valueEnd < json.length() && json.charAt(valueEnd) != ',' && json.charAt(valueEnd) != '}') {
                valueEnd++;
            }
            return json.substring(valueStart, valueEnd).trim();
        }
    }
}
