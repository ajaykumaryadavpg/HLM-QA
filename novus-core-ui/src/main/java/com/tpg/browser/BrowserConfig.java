package com.tpg.browser;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.tpg.annotations.ParallelThreadScope;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class BrowserConfig {

    @Value("${browser.headless}")
    private boolean headless;
    @Value("${browser.width}")
    private int width;
    @Value("${browser.height}")
    private int height;
    @Value("${browser.slowmo}")
    private int slowMo;
    @Value("${browser.headless.args}")
    private String newHeadless;
    @Value("${browser.ignore.args}")
    private String ignoreArgs;
    @Value("${browser.fullscreen}")
    private String startFullScreen;
    @Value("${browser.fullscreen.enabled}")
    private boolean fullScreenEnabled;
    @Autowired
    Playwright driver;


    @ParallelThreadScope
    public Page getDriver() {
        List<String> args = new ArrayList<>();
        Browser.NewContextOptions options = new Browser.NewContextOptions();
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions().setHeadless(headless);
        if (!StringUtils.isAllEmpty(newHeadless)) args.add(newHeadless);
        if (fullScreenEnabled) {
            args.add(startFullScreen);
            options.setViewportSize(null);
        } else options.setViewportSize(width, height);
        if (!StringUtils.isAllEmpty(ignoreArgs)) launchOptions.setIgnoreDefaultArgs(List.of(ignoreArgs));
        var chrome = driver.chromium().launch(launchOptions.setHeadless(false).setSlowMo(slowMo).setArgs(args));
        var tab = chrome.newContext(options);
        return tab.newPage();
    }
}
