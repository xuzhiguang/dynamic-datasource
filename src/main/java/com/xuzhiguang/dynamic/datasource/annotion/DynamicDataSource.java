package com.xuzhiguang.dynamic.datasource.annotion;

import java.lang.annotation.*;

/**
 * @author xuzhiguang
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DynamicDataSource {

    String value();

}
