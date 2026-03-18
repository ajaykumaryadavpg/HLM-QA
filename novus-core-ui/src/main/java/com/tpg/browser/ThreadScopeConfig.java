package com.tpg.browser;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ThreadScopeConfig {

    @Bean
    public BeanFactoryPostProcessor threadPostProcessor() {
        return new ThreadScopePostProcessor();
    }
}
