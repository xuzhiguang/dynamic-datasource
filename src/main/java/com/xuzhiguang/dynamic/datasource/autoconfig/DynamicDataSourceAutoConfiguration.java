package com.xuzhiguang.dynamic.datasource.autoconfig;

import com.xuzhiguang.dynamic.datasource.DynamicDataSourceContext;
import com.xuzhiguang.dynamic.datasource.DynamicRoutingDataSource;
import com.xuzhiguang.dynamic.datasource.annotion.DynamicDataSource;
import com.xuzhiguang.dynamic.datasource.aop.DynamicDataSourceAdvice;
import com.xuzhiguang.dynamic.datasource.constant.DynamicDataSourceConstant;
import com.xuzhiguang.dynamic.datasource.ds.DataSourceCreator;
import com.xuzhiguang.dynamic.datasource.ds.DynamicDataSourceProperties;
import com.xuzhiguang.dynamic.datasource.listener.DynamicDataSourceChangeListener;
import org.aopalliance.aop.Advice;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.annotation.AnnotationMatchingPointcut;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Role;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * @author xuzhiguang
 */
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@AutoConfigureBefore(DataSourceAutoConfiguration.class)
@EnableConfigurationProperties({DynamicDataSourceProperties.class})
@ConditionalOnProperty(prefix = DynamicDataSourceConstant.PROPERTIES_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
public class DynamicDataSourceAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public DataSourceCreator dataSourceCreator() {
        return new DataSourceCreator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicRoutingDataSource dynamicDataSource(DataSourceCreator dataSourceCreator, DynamicDataSourceProperties dynamicDataSourceProperties) {

        DynamicDataSourceContext.setDefaultKey(dynamicDataSourceProperties.getDefaultKey());

        DynamicRoutingDataSource dynamicRoutingDataSource = new DynamicRoutingDataSource();
        Map<String, DataSourceProperties> map = dynamicDataSourceProperties.getDatasource();
        Assert.notEmpty(map, "spring.xzg.dynamic.datasource must not be empty");
        Assert.hasText(dynamicDataSourceProperties.getDefaultKey(), "spring.xzg.dynamic.default-key must not be empty");
        Assert.isTrue(map.containsKey(dynamicDataSourceProperties.getDefaultKey()),
                "spring.xzg.dynamic.default-key must be defined in spring.xzg.dynamic.datasource");
        map.forEach((key, properties) -> dynamicRoutingDataSource.addDataSource(key, dataSourceCreator.create(properties)));

        return dynamicRoutingDataSource;
    }

    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Bean
    public Advisor dynamicDataSourceAdvisor() {

        Pointcut pointcut = new AnnotationMatchingPointcut(null, DynamicDataSource.class);
        Advice advice = new DynamicDataSourceAdvice();
        return new DefaultPointcutAdvisor(pointcut, advice);
    }

    @Bean
    @ConditionalOnClass(name = "org.springframework.cloud.context.config.annotation.RefreshScope")
    @ConditionalOnProperty(prefix = DynamicDataSourceConstant.LISTENER_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public DynamicDataSourceChangeListener dynamicDataSourceChangeListener(DynamicDataSourceProperties dynamicDataSourceProperties,
                                                                           DynamicRoutingDataSource dynamicRoutingDataSource,
                                                                           DataSourceCreator dataSourceCreator) {
        return new DynamicDataSourceChangeListener(dynamicDataSourceProperties, dynamicRoutingDataSource, dataSourceCreator);
    }

}
