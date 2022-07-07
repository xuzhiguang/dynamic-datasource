package com.xuzhiguang.dynamic.datasource.listener;

import com.xuzhiguang.dynamic.datasource.DynamicRoutingDataSource;
import com.xuzhiguang.dynamic.datasource.DynamicDataSourceContext;
import com.xuzhiguang.dynamic.datasource.constant.DynamicDataSourceConstant;
import com.xuzhiguang.dynamic.datasource.ds.DataSourceCreator;
import com.xuzhiguang.dynamic.datasource.ds.DynamicDataSourceProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.SmartApplicationListener;

import javax.sql.DataSource;
import java.util.HashSet;
import java.util.Set;

/**
 * @author xuzhiguang
 */
@Slf4j
public class DynamicDataSourceChangeListener implements SmartApplicationListener {

    private final static String PREFIX = DynamicDataSourceConstant.PROPERTIES_PREFIX + ".datasource.";

    private final DynamicDataSourceProperties dynamicDataSourceProperties;

    private final DynamicRoutingDataSource dynamicRoutingDataSource;

    private final DataSourceCreator dataSourceCreator;

    private final Set<String> dataSourceKeySet = new HashSet<>();

    private boolean defaultKeyChanged = false;

    public DynamicDataSourceChangeListener(DynamicDataSourceProperties dynamicDataSourceProperties,
                                           DynamicRoutingDataSource dynamicRoutingDataSource) {
        this(dynamicDataSourceProperties, dynamicRoutingDataSource, new DataSourceCreator());
    }

    public DynamicDataSourceChangeListener(DynamicDataSourceProperties dynamicDataSourceProperties,
                                           DynamicRoutingDataSource dynamicRoutingDataSource,
                                           DataSourceCreator dataSourceCreator) {
        this.dynamicDataSourceProperties = dynamicDataSourceProperties;
        this.dynamicRoutingDataSource = dynamicRoutingDataSource;
        this.dataSourceCreator = dataSourceCreator;
    }


    @Override
    public boolean supportsEventType(Class<? extends ApplicationEvent> eventType) {
        return EnvironmentChangeEvent.class.isAssignableFrom(eventType)
                || RefreshScopeRefreshedEvent.class.isAssignableFrom(eventType);
    }

    @Override
    public synchronized void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof EnvironmentChangeEvent) {
            handleEnvironmentChange((EnvironmentChangeEvent) event);
        } else if (event instanceof RefreshScopeRefreshedEvent) {
            handleRefreshScopeRefreshed((RefreshScopeRefreshedEvent) event);
        }
    }

    private void handleEnvironmentChange(EnvironmentChangeEvent event) {
        Set<String> keys = event.getKeys();

        keys.forEach(key -> {
            if (key.startsWith(PREFIX)) {
                String dataSourceKey = obtainDataSourceKey(key);
                if (!dataSourceKeySet.contains(dataSourceKey)) {
                    dataSourceKeySet.add(dataSourceKey);
                    log.info("DynamicDataSource - '{}' changed", dataSourceKey);
                }
            } else if ((DynamicDataSourceConstant.PROPERTIES_PREFIX + ".default-key").equals(key)) {
                defaultKeyChanged = true;
                log.info("DynamicDataSource - default key changed");
            }
        });
        log.info("DynamicDataSource - wait refresh scope complete");
    }

    private String obtainDataSourceKey(String changeKey) {
        int pointIndex = changeKey.indexOf(".", PREFIX.length());
        if (pointIndex < 0) {
            return changeKey.substring(PREFIX.length());
        }
        return changeKey.substring(PREFIX.length(), pointIndex);
    }

    private void handleRefreshScopeRefreshed(RefreshScopeRefreshedEvent event) {
        if (defaultKeyChanged) {
            DynamicDataSourceContext.setDefaultKey(dynamicDataSourceProperties.getDefaultKey());
            defaultKeyChanged = false;
        }
        dataSourceKeySet.forEach(this::refreshDataSource);
        dataSourceKeySet.clear();
    }

    private void refreshDataSource(String dataSourceKey) {
        DataSourceProperties dataSourceProperties = dynamicDataSourceProperties.getDatasource() == null ? null :
                dynamicDataSourceProperties.getDatasource().get(dataSourceKey);
        if (dataSourceProperties == null) {
            dynamicRoutingDataSource.removeDataSource(dataSourceKey);
            return;
        }
        DataSource newDataSource = dataSourceCreator.create(dataSourceProperties);
        dynamicRoutingDataSource.addDataSource(dataSourceKey, newDataSource);
    }
}
