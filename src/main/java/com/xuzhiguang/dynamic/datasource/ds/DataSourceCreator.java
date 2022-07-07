package com.xuzhiguang.dynamic.datasource.ds;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import javax.sql.DataSource;

/**
 * @author xuzhiguang
 */
public class DataSourceCreator {

    public DataSource create(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }
}
