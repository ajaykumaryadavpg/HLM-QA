package com.tpg.automation.example;

import com.tpg.NovusApiTestBase;
import com.tpg.annotations.Description;
import com.tpg.automation.listeners.NovusTestListener;
import com.tpg.automation.pages.home.HomePage;
import com.tpg.methods.Delete;
import com.tpg.methods.Get;
import com.tpg.methods.Post;
import com.tpg.methods.Put;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.tpg.automation.constants.TestGroups.SMOKE_TESTS;

@ActiveProfiles({"test"})
@Listeners(NovusTestListener.class)
public class BasicApiTests extends NovusApiTestBase {

    @Description("Dummy Tests dont run them")
    @Test(groups = SMOKE_TESTS)
    public void basicGetApiTest() {
        Get.atUrl("some url")
            .withBasicAuth("username", "password")
            .withParam("key", "value")
            .withParam("key", "value")
            .execute()
            .printResponse()
            .isOk()
            .mapToObject(HomePage.class); //HomePage should be a DTO/Java Record/POJO
    }

    @Description("Dummy Tests dont run them")
    @Test(groups = SMOKE_TESTS)
    public void basicPostApiTest() {
        Post.atUrl("some url")
            .withBasicAuth("username", "password")
            .withBody("json string")
            .execute()
            .isOk();
    }

    @Description("Dummy Tests dont run them")
    @Test(groups = SMOKE_TESTS)
    public void basicPutApiTest() {
        Put.atUrl("some url")
            .withBasicAuth("username", "password")
            .withBody("json string")
            .execute()
            .isNotOk();
    }

    @Description("Dummy Tests dont run them")
    @Test(groups = SMOKE_TESTS)
    public void basicDeleteApiTest() {
        Delete.atUrl("some url")
            .withBasicAuth("username", "password")
            .withBody("json string")
            .execute()
            .statusCodeMatches(201);
    }
}
