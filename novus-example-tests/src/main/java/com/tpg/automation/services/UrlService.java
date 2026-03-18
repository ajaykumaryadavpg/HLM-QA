package com.tpg.automation.services;

import com.tpg.services.BaseUrlService;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(value = "thread-scope", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UrlService extends BaseUrlService {

    @Override public String baseUrl() {
        return protocol + domain;
    }
}
