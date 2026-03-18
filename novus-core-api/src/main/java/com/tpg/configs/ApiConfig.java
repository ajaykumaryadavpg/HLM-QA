package com.tpg.configs;

import com.microsoft.playwright.APIRequest;
import org.springframework.stereotype.Component;

@Component
public class ApiConfig {

    public APIRequest.NewContextOptions configs() {
        return new APIRequest.NewContextOptions().setIgnoreHTTPSErrors(true).setTimeout(100000);
    }
}
