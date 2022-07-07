package com.xuzhiguang.dynamic.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xuzhiguang
 */
@Slf4j
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final Map<String, DataSource> dataSourceMap = new ConcurrentHashMap<>();

    @Override
    protected Object determineCurrentLookupKey() {
        return DynamicDataSourceContext.getCurrentDataSourceKey();
    }

    public synchronized void addDataSource(String key, DataSource datasource) {

        DataSource dataSource = dataSourceMap.put(key, datasource);
        refreshDataSources();
        if (dataSource != null) {
            tryCloseDataSource(dataSource);
        } else {
            DynamicDataSourceContext.addDataSourceKey(key);
        }
        log.info("DynamicDataSource - datasource '{}' added", key);
    }

    public synchronized void removeDataSource(String key) {

        DataSource dataSource = dataSourceMap.remove(key);
        refreshDataSources();
        DynamicDataSourceContext.removeDataSourceKey(key);
        if (dataSource != null) {
            tryCloseDataSource(dataSource);
        }
        log.info("DynamicDataSource - datasource '{}' removed", key);
    }

    public DataSource getDataSource(String key) {
        return dataSourceMap.get(key);
    }

    private void refreshDataSources() {
        this.setTargetDataSources(new HashMap<>(dataSourceMap));
        this.afterPropertiesSet();
    }

    private void tryCloseDataSource(DataSource dataSource) {
        try {
            if (dataSource instanceof AutoCloseable) {
                ((AutoCloseable) dataSource).close();
            }
        } catch (Exception e) {
            log.warn("DynamicDataSource - close datasource failed", e);
        }
    }

}
