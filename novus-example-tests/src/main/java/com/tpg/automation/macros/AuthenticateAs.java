package com.tpg.automation.macros;

import com.tpg.actions.Perform;
import com.tpg.actions.Performable;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.tpg.automation.impls.inventory.LoginPageImpl.clickLoginButton;
import static com.tpg.automation.impls.inventory.LoginPageImpl.enterEmail;
import static com.tpg.automation.impls.inventory.LoginPageImpl.enterPassword;

/**
 * Reusable login macro for the Inventory Management application.
 *
 * <p>Usage:
 * <pre>
 *   user.attemptsTo(
 *       AuthenticateAs.aUser()
 *           .withUsername("admin")
 *           .withPassword("Admin@123")
 *           .andLogin()
 *   );
 * </pre>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthenticateAs {

    private String username;
    private String password;

    /** Entry point – returns a builder-style instance. */
    public static AuthenticateAs aUser() {
        return new AuthenticateAs();
    }

    public AuthenticateAs withUsername(String username) {
        this.username = username;
        return this;
    }

    public AuthenticateAs withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Composes username entry, password entry, and login-button click into a
     * single {@link Performable} that can be passed to {@code actor.attemptsTo()}.
     */
    public Performable andLogin() {
        return Perform.actions(
                enterEmail(username),
                enterPassword(password),
                clickLoginButton()
        ).log("AuthenticateAs#andLogin",
                "Authenticates as '" + username + "' on the HLM Platform login page");
    }
}
