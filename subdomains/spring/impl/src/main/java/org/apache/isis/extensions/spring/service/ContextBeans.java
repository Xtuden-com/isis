package org.apache.isis.extensions.spring.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;

import lombok.Data;

@Data
public final class ContextBeans {

    private final Map<String, BeanDescriptor> beans;
    private final String parentId;
    private final ConfigurableApplicationContext context;

    static ContextBeans describing(final ConfigurableApplicationContext context) {
        if (context == null) {
            return null;
        }

        final ConfigurableApplicationContext parent = SpringBeansService.Util.getConfigurableParent(context);
        final Map<String, BeanDescriptor> beans = describeBeans(context);
        final String parentId = parent != null ? parent.getId() : null;
        return new ContextBeans(beans, parentId, context);
    }

    private static Map<String, BeanDescriptor> describeBeans(final ConfigurableApplicationContext context) {

        final ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();

        final Map<String, BeanDescriptor> beans = Arrays.stream(beanFactory.getBeanDefinitionNames())
                .filter(beanName -> isBeanEligible(beanName, beanFactory))
                .collect(Collectors.toMap(Function.identity(), beanName -> new BeanDescriptor(beanName, context)));
        return Collections.unmodifiableMap(beans);
    }

    private static boolean isBeanEligible(
            final String beanName,
            final ConfigurableListableBeanFactory bf) {
        final BeanDefinition bd = bf.getBeanDefinition(beanName);
        return bd.getRole() != BeanDefinition.ROLE_INFRASTRUCTURE && (!bd.isLazyInit() || bf.containsSingleton(beanName));
    }
}