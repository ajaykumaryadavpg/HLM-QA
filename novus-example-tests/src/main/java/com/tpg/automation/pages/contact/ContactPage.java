package com.tpg.automation.pages.contact;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactPage {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LetsTalk {
        public static final String FIRST_NAME = LocateBy.id("FirstName");
        public static final String LAST_NAME = LocateBy.name("LastName");
        public static final String COMPANY_NAME = LocateBy.id("Company");
        public static final String BIZ_EMAIL = LocateBy.id("Email");
        public static final String BIZ_PHONE = LocateBy.id("Phone");
        public static final String JOB_TITLE = LocateBy.id("Title");
        public static final String LOCATION_DPDWN = LocateBy.id("State");
        public static final String MESSAGE = LocateBy.id("commentCapture");
        public static final String LETS_TALK_BTN = LocateBy.withExactCssText("button", "LET'S TALK");
    }
}
