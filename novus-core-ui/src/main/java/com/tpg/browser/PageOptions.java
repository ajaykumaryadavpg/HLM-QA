package com.tpg.browser;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.tpg.annotations.Prototype;

@Prototype
public class PageOptions {

    public Page.NavigateOptions getDefaultSetupOptions() {
        return new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED).setWaitUntil(WaitUntilState.LOAD).setTimeout(40000);
    }
}
