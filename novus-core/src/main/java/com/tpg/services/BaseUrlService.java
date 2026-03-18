package com.tpg.services;

import org.springframework.beans.factory.annotation.Value;

public abstract class BaseUrlService {

    @Value("${aut.protocol}")
    protected String protocol;
    @Value("${aut.domain}")
    protected String domain;

    public abstract String baseUrl();
}
