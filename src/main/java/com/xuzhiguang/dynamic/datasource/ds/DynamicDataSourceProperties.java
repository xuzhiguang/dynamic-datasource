package com.xuzhiguang.dynamic.datasource.ds;

import com.xuzhiguang.dynamic.datasource.constant.DynamicDataSourceConstant;
import lombok.Data;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author xuzhiguang
 */
@Data
@ConfigurationProperties(prefix = DynamicDataSourceConstant.PROPERTIES_PREFIX)
public class DynamicDataSourceProperties {


    private String defaultKey;

    private Map<String, DataSourceProperties> datasource;

}
