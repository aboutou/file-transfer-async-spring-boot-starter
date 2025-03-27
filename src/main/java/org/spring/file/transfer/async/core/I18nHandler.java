package org.spring.file.transfer.async.core;

/**
 * 国际化接口
 * @author wuzhencheng
 */
@FunctionalInterface
public interface I18nHandler {

    /**
     * 获取当前名称
     *
     * @param name 注解配置的
     * @return 返回国际化的名字
     */
    String getLocaleName(String name);
}
