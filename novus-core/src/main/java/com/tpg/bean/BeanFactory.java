package com.tpg.bean;

import com.github.javafaker.Faker;
import com.github.javafaker.service.RandomService;
import com.microsoft.playwright.Playwright;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.*;

import java.util.Locale;

@Configuration
public class BeanFactory {

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    @Lazy
    public Faker faker() {
        return new Faker(new Locale("en-US"), new RandomService());
    }

    @Bean
    @Scope(value = "thread-scope", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Playwright playwright() {
        return Playwright.create();
    }
}
