package org.spring.file.transfer.async.domain.events;

import com.github.phantomthief.pool.KeyAffinityExecutor;
import lombok.extern.slf4j.Slf4j;
//#import org.apache.logging.log4j2.threadpool.Log4j2Callable;
//#import org.apache.logging.log4j2.threadpool.Log4j2Runnable;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.ErrorHandler;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午10:20
 */
@Slf4j
public class TaskApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private final KeyAffinityExecutor executorService;

    public TaskApplicationEventMulticaster(Executor executor) {
        this(null, executor, null);
    }

    public TaskApplicationEventMulticaster(KeyAffinityExecutor executorService, ErrorHandler errorHandler) {
        this(executorService, null, errorHandler);
    }

    public TaskApplicationEventMulticaster(KeyAffinityExecutor executorService, Executor executor, ErrorHandler errorHandler) {
        super.setTaskExecutor(executor);
        super.setErrorHandler(errorHandler);
        this.executorService = executorService;
    }

    @Override
    public void multicastEvent(ApplicationEvent event, ResolvableType eventType) {
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        if (event instanceof AbstractAsyncEvent) {
            AbstractAsyncEvent asyncEvent = (AbstractAsyncTaskEvent) event;
            KeyAffinityExecutor hashKeyExecutor = executorService;
            Executor executor = getTaskExecutor();
            for (ApplicationListener<?> listener : getApplicationListeners(event, type)) {
                if (asyncEvent.isAsync()) {
                    if (asyncEvent instanceof AbstractAsyncHashKeyEvent) {
                        AbstractAsyncHashKeyEvent hashKeyEvent = (AbstractAsyncHashKeyEvent) asyncEvent;
                        hashKeyExecutor.submit(hashKeyEvent.getHashKey(), (() -> {
                            executeListener(listener, event);
                            return null;
                        }));
                    } else {
                        executor.execute((() -> executeListener(listener, event)));
                    }
                } else {
                    executeListener(listener, event);
                }
            }
        } else {
            super.multicastEvent(event, type);
        }
    }

    private void executeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        invokeListener(listener, event);
    }


    @Override
    protected void invokeListener(ApplicationListener<?> listener, ApplicationEvent event) {
        if (!(event instanceof AbstractRetryEvent)) {
            super.invokeListener(listener, event);
            return;
        }
        AbstractRetryEvent retryEvent = (AbstractRetryEvent) event;
        // 创建 RetryTemplate 实例
        RetryTemplate retryTemplate = new RetryTemplate();
        // 配置重试策略（例如，最大重试次数为3）
        RetryPolicy retryPolicy = new SimpleRetryPolicy(retryEvent.getMaxAttempts());
        // 配置重试的等待间隔（例如，每次重试之间等待1秒）
        FixedBackOffPolicy backOffPolicy = new FixedBackOffPolicy();
        backOffPolicy.setBackOffPeriod(retryEvent.getIntervalTime());
        retryTemplate.setRetryPolicy(retryPolicy);
        retryTemplate.setBackOffPolicy(backOffPolicy);

        // 通过RecoveryCallback 重试流程正常结束或者达到重试上限后的退出恢复操作实例
        final RecoveryCallback<Void> recoveryCallback = context -> {
            log.info("重试{}次，任务执行结束是失败", context.getRetryCount());
            BiConsumer<AbstractRetryEvent, Throwable> failOnCallback = retryEvent.getFailOnCallback();
            Throwable lastThrowable = context.getLastThrowable();
            if (failOnCallback != null) {
                failOnCallback.accept(retryEvent, lastThrowable);
            }
            throw new RuntimeException(lastThrowable);
        };
        // 通过RetryCallback 重试回调实例包装正常逻辑逻辑，第一次执行和重试执行执行的都是这段逻辑
        RetryCallback<Void, Exception> retryCallback = context -> {
            TaskApplicationEventMulticaster.super.invokeListener(listener, event);
            return null;
        };
        try {
            retryTemplate.execute(retryCallback, recoveryCallback);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            throw new RuntimeException(t);
        }
    }
}
