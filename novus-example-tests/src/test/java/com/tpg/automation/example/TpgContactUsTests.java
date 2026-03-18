package com.tpg.automation.example;

import com.tpg.NovusGuiTestBase;
import com.tpg.actions.Launch;
import com.tpg.actor.Actor;
import com.tpg.annotations.Description;
import com.tpg.annotations.MetaData;
import com.tpg.annotations.Outcome;
import com.tpg.automation.listeners.NovusTestListener;
import com.tpg.automation.macros.Navigate;
import com.tpg.automation.services.UrlService;
import com.tpg.verification.Verify;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static com.tpg.automation.impls.contact.ContactPageImpl.*;
import static com.tpg.automation.pages.home.HomePage.CONTACT_LINK;
import static com.tpg.utils.CodeFillers.on;

@ActiveProfiles({"web", "test", "local"})
@Listeners(NovusTestListener.class)
public class TpgContactUsTests extends NovusGuiTestBase {

    @Autowired
    UrlService urlService;
    Actor client = new Actor();

    @MetaData(author = "Sidhant Satapathy", testCaseId = "1", stories = {"JIRA-1234", "JIRA-1245"}, category = "TPG_CONTACT_US")
    @Description("Test Lets Talk functionality on the Contact Page")
    @Outcome("Verify that the clients can write to us with all the details successfully")
    @Test
    public void testClientWritingToUs() {
        step("Client launches 3pillar website");
        client.attemptsTo(Launch.app(on(urlService.baseUrl())).withConfigs(pageOptions.getDefaultSetupOptions()));

        // hard assertion
        client.wantsTo(Verify.uiElement(CONTACT_LINK).describedAs("contact link is displayed").isVisible());

        step("Client goes to the \"Contact\" page on the 3pillar website");
        client.attemptsTo(
            Navigate.to().contactPage(),
            fillFirstName("Test"),
            fillLastName("User"),
            fillCompanyName("Test Company"),
            fillBizEmail("client@abc.com"),
            fillBizPhoneNumber("1234567890"),
            fillJobTitle("job title"),
            selectState("Alabama"),
            fillClientMessage("Hi I am testing the Lets Talk Section, Hope you got my email"));
    }
}
