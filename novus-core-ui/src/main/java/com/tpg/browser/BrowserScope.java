package com.tpg.browser;

import com.microsoft.playwright.Page;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.support.SimpleThreadScope;

public class BrowserScope extends SimpleThreadScope {

    @Override public Object get(String name, ObjectFactory<?> objectFactory) {
        Object o = super.get(name, objectFactory);
        if (!((Page) o).context().browser().isConnected()) {
            super.remove(name);
            o = super.get(name, objectFactory);
        }
        return o;
    }

    @Override public void registerDestructionCallback(String name, Runnable callback) {
    }
}
