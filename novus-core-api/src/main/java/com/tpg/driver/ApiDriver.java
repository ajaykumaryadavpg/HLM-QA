package com.tpg.driver;

import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.Playwright;
import com.tpg.configs.ApiConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ApiDriver {

    @Autowired
    ApiConfig config;

    @Autowired
    Playwright playwright;

    public APIRequestContext context() {
        return playwright.request().newContext(config.configs());
    }
}
