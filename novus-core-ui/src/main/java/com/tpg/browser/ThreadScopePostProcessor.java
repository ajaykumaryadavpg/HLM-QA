package com.tpg.browser;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.support.SimpleThreadScope;

public class ThreadScopePostProcessor implements BeanFactoryPostProcessor {

    @Override public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        beanFactory.registerScope("thread-scope", new SimpleThreadScope());
    }
}
