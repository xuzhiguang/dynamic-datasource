package com.xuzhiguang.dynamic.datasource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xuzhiguang
 */
public class DynamicDataSourceContext {

    private static final Set<String> DATASOURCE_KEYS = Collections.synchronizedSet(new HashSet<>());

    private static volatile String DEFAULT_KEY = null;

    private static final ThreadLocal<String> CURRENT_DATASOURCE_KEY = new ThreadLocal<>();

    public static String getCurrentDataSourceKey() {

        String currentDataSourceKey = CURRENT_DATASOURCE_KEY.get();
        return currentDataSourceKey != null ? currentDataSourceKey : DEFAULT_KEY;
    }

    public synchronized static void addDataSourceKey(String key) {
        DATASOURCE_KEYS.add(key);
    }

    public synchronized static void removeDataSourceKey(String key) {
        DATASOURCE_KEYS.remove(key);
    }

    public static void setCurrentDataSourceKey(String key) {
        if (!DATASOURCE_KEYS.contains(key)) {
            throw new IllegalArgumentException("datasource key not found");
        }
        CURRENT_DATASOURCE_KEY.set(key);
    }
    public static void setDefaultKey(String key) {
        DEFAULT_KEY = key;
    }

    public static void removeCurrentDataSourceKey() {
        CURRENT_DATASOURCE_KEY.remove();
    }

}
