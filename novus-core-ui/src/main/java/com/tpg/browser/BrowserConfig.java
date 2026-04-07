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

import java.nio.file.Paths;
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
    @Value("${browser.executable.path:}")
    private String executablePath;
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
        if (!StringUtils.isAllBlank(executablePath)) launchOptions.setExecutablePath(Paths.get(executablePath));
        var chrome = driver.chromium().launch(launchOptions.setHeadless(false).setSlowMo(slowMo).setArgs(args));
        var tab = chrome.newContext(options);
        tab.addInitScript("() => {\n" +
                "  const cursor = document.createElement('div');\n" +
                "  cursor.style.cssText = 'position:fixed;z-index:2147483647;top:0;left:0;width:20px;height:20px;" +
                "border-radius:50%;background:rgba(255,165,0,0.6);border:2px solid orange;" +
                "pointer-events:none;transform:translate(-50%,-50%);transition:left 0.05s,top 0.05s;';\n" +
                "  document.documentElement.appendChild(cursor);\n" +
                "  document.addEventListener('mousemove', e => {\n" +
                "    cursor.style.left = e.clientX + 'px';\n" +
                "    cursor.style.top = e.clientY + 'px';\n" +
                "  });\n" +
                "}");
        return tab.newPage();
    }
}
