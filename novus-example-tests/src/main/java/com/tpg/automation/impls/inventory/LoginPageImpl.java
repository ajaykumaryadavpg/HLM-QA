package com.tpg.automation.impls.inventory;

import com.tpg.actions.Clear;
import com.tpg.actions.Click;
import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import com.tpg.actions.Type;
import com.tpg.automation.pages.inventory.LoginPage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginPageImpl {

    public static Performable enterEmail(String email) {
        return Perform.actions(
                Type.text(email).on(LoginPage.EMAIL_FIELD)
        ).log("enterEmail", "Types email address into the login email field");
    }

    public static Performable enterPassword(String password) {
        return Perform.actions(
                Type.text(password).on(LoginPage.PASSWORD_FIELD)
        ).log("enterPassword", "Types password into the login password field");
    }

    public static Performable clickLoginButton() {
        return Perform.actions(
                Click.on(LoginPage.LOGIN_BUTTON)
        ).log("clickLoginButton", "Clicks the Sign in submit button");
    }

    public static Performable clearEmail() {
        return Perform.actions(
                Clear.locator(LoginPage.EMAIL_FIELD)
        ).log("clearEmail", "Clears the email address field");
    }

    public static Performable clearPassword() {
        return Perform.actions(
                Clear.locator(LoginPage.PASSWORD_FIELD)
        ).log("clearPassword", "Clears the password field");
    }
}
