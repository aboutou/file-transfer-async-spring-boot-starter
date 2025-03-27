package org.spring.file.transfer.async.domain.service;

import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午5:51
 */
public class SpringContextService implements ApplicationContextAware, DisposableBean {


    /**
     * 实现ApplicationContextAware接口, 注入Context到静态变量中.
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        SpringContextHolderUtil.setApplicationContext(applicationContext);
    }

    /**
     * 实现DisposableBean接口, 在Context关闭时清理静态变量.
     */
    @Override
    public void destroy() {
        SpringContextHolderUtil.clearHolder();
    }
}
