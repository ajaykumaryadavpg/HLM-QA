package com.tpg.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LocalCache {
    private static final ThreadLocal<Map<String, Object>> cache = new ThreadLocal<>();

    public static void cache(String key, Object value) {
        var cacheMap = new HashMap<String, Object>();
        if (cache.get() == null) {
            cacheMap.put(key, value);
            cache.set(cacheMap);
        }
        cache.get().put(key, value);
    }

    public static Object fetch(String key) {
        return cache.get().get(key);
    }

    public static void remove() {
        cache.remove();
    }
}
