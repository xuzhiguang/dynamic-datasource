package com.xuzhiguang.dynamic.datasource.aop;

import com.xuzhiguang.dynamic.datasource.DynamicDataSourceContext;
import com.xuzhiguang.dynamic.datasource.annotion.DynamicDataSource;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.Ordered;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.lang.reflect.Method;

/**
 * @author xuzhiguang
 */
public class DynamicDataSourceAdvice implements MethodInterceptor, Ordered {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();

    private static final ExpressionParser PARSER = new SpelExpressionParser();

    private static final String SPEL_PREFIX = "#";

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {

        try {
            String dataSourceKey = obtainDataSourceKey(invocation);
            DynamicDataSourceContext.setCurrentDataSourceKey(dataSourceKey);
            return invocation.proceed();
        } finally {
            DynamicDataSourceContext.removeCurrentDataSourceKey();
        }
    }

    /**
     * 获得数据源
     * @param invocation
     * @return
     */
    private String obtainDataSourceKey(MethodInvocation invocation) {

        Method method = invocation.getMethod();

        DynamicDataSource dynamicDataSource = method.getAnnotation(DynamicDataSource.class);

        if (dynamicDataSource == null) {
            return null;
        }

        String expression = dynamicDataSource.value();
        if (!expression.startsWith(SPEL_PREFIX)) {
            return expression;
        } else {
            EvaluationContext context = new MethodBasedEvaluationContext(null, method, invocation.getArguments(), NAME_DISCOVERER);
            return PARSER.parseExpression(expression).getValue(context, String.class);
        }
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE - 1;
    }
}
