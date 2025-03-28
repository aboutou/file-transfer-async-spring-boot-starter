package org.spring.file.transfer.async.utils;

import org.apache.commons.collections4.MapUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午5:54
 */
public abstract class SpringContextHolderUtil {

    private static ApplicationContext applicationContext = null;

    private static String appName = null;

    /**
     * 取得存储在静态变量中的ApplicationContext.
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolderUtil.applicationContext = applicationContext;
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        return (T) applicationContext.getBean(name);
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 从静态变量applicationContext中取得Bean, 自动转型为所赋值对象的类型.
     */
    public static <T> List<T> getBeans(Class<T> requiredType) {
        Map<String, T> beansOfType = applicationContext.getBeansOfType(requiredType);
        if (MapUtils.isNotEmpty(beansOfType)) {
            return new ArrayList<>(beansOfType.values());
        }
        return null;
    }

    /**
     * 清除SpringContextHolder中的ApplicationContext为Null.
     */
    public static void clearHolder() {
        applicationContext = null;
    }

    public static String getAppName() {
        if (appName == null) {
            appName = applicationContext.getEnvironment().getProperty("spring.application.name");
        }
        return appName;
    }

    /**
     * 发布事件
     *
     * @param event
     */
    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext == null) {
            return;
        }
        applicationContext.publishEvent(event);
    }
}
