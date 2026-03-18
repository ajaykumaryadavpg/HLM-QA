package com.tpg.automation.pages.inventory;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Page object for the HLM Platform login page.
 *
 * Locators verified via browser inspection against:
 * https://main.dddsig2mih3hw.amplifyapp.com/login
 *
 * Key findings:
 *  - Email input  : id="email"
 *  - Password     : id="password"
 *  - Submit button: <button type="submit"> text "Sign in"
 *  - Error banner : <p class="text-red-400 text-sm"> inside a red container div
 *  - No separate field-level error elements – app uses a single banner
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginPage {

    /** Email address input field – DOM id="email" */
    public static final String EMAIL_FIELD    = LocateBy.id("email");

    /** Password input field – DOM id="password" */
    public static final String PASSWORD_FIELD = LocateBy.id("password");

    /**
     * Sign-in submit button.
     * type=submit, inner-text "Sign in" (NOT "Login").
     */
    public static final String LOGIN_BUTTON   = LocateBy.withExactCssText("button[type='submit']", "Sign in");

    /**
     * Authentication error paragraph.
     * Appears inside a red-tinted banner; class "text-red-400 text-sm".
     */
    public static final String ERROR_MESSAGE  = LocateBy.css("p.text-red-400");

    /** Login page heading – "Sign in to your account" */
    public static final String PAGE_TITLE     = LocateBy.withCssText("h2", "Sign in to your account");
}
