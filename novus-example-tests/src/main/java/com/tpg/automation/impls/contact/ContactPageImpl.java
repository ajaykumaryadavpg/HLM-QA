package com.tpg.automation.impls.contact;

import com.tpg.actions.Enter;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.actions.Select;
import com.tpg.automation.pages.contact.ContactPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContactPageImpl {

    public static Performable fillFirstName(String fName) {
        return Perform.actions(Enter.text(fName).on(ContactPage.LetsTalk.FIRST_NAME)).log("fillFirstName", "fills the first name of the client");
    }

    public static Performable fillLastName(String lName) {
        return Perform.actions(Enter.text(lName).on(ContactPage.LetsTalk.LAST_NAME)).log("fillLastName", "fills the last name of the client");
    }

    public static Performable fillCompanyName(String cName) {
        return Perform.actions(Enter.text(cName).on(ContactPage.LetsTalk.COMPANY_NAME)).log("fillCompanyName", "fills the company name of the client");
    }

    public static Performable fillBizEmail(String bizEmail) {
        return Perform.actions(Enter.text(bizEmail).on(ContactPage.LetsTalk.BIZ_EMAIL)).log("fillBizEmail", "fills the biz email of the client");
    }

    public static Performable fillBizPhoneNumber(String phNumber) {
        return Perform.actions(Enter.text(phNumber).on(ContactPage.LetsTalk.BIZ_PHONE)).log("fillBizPhoneNumber", "fills the biz phone number of the client");
    }

    public static Performable fillJobTitle(String jobTitle) {
        return Perform.actions(Enter.text(jobTitle).on(ContactPage.LetsTalk.JOB_TITLE)).log("fillJobTitle", "fills the job title of the client");
    }

    public static Performable fillClientMessage(String message) {
        return Perform.actions(Enter.text(message).on(ContactPage.LetsTalk.MESSAGE)).log("fillClientMessage", "fills the client's message");
    }

    public static Performable selectState(String state) {
        return Perform.actions(Select.option(state).on(ContactPage.LetsTalk.LOCATION_DPDWN)).log("selectState", "fills the client's state");
    }
}
