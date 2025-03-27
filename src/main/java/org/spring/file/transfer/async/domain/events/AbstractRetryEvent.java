package org.spring.file.transfer.async.domain.events;


import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.function.BiConsumer;

/**
 * @author wuzhencheng
 */
@Getter
@Setter
public class AbstractRetryEvent extends ApplicationEvent {

    /**
     * 失败回调方法
     */
    private BiConsumer<AbstractRetryEvent, Throwable> failOnCallback;

    /**
     * 重试次数
     */
    private final int maxAttempts;

    /**
     * 间隔时间
     */
    private long intervalTime = 1000L;

    public AbstractRetryEvent(Object source, int maxAttempts) {
        super(source);
        this.maxAttempts = maxAttempts;
    }

}
