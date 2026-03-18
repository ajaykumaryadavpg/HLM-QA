package com.tpg.automation.impls.home;

import com.tpg.actions.Click;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.tpg.automation.pages.home.HomePage.CONTACT_LINK;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HomePageImpl {

    public static Performable goToContactPage() {
        return Perform.actions(
            Click.on(CONTACT_LINK)
        ).log("goToContactPage", "Go to contact page");
    }

}
