package com.tpg.automation.pages.home;

import com.tpg.locatorstrategy.LocateBy;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HomePage {

    public static final String CONTACT_LINK = LocateBy.withExactCssText(".header-contact.header-search a", "Contact");
}
