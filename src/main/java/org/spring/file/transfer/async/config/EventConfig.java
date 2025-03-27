package org.spring.file.transfer.async.config;

import com.github.phantomthief.pool.KeyAffinityExecutor;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.TaskDataPartitionValue;
import org.spring.file.transfer.async.domain.events.TaskApplicationEventMulticaster;
import org.spring.file.transfer.async.domain.events.TaskChanagedEventListener;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.Optional;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午10:31
 */
@Slf4j
@AutoConfigureBefore(TaskExecutionAutoConfiguration.class)
public class EventConfig {

    public static final String TASK_FILE_EVENT_EXECUTOR_NAME = "eventTaskExecutor";

    @Bean
    public TaskChanagedEventListener taskChanagedEventListener() {
        return new TaskChanagedEventListener();
    }

    @Bean
    public KeyAffinityExecutor keyAffinityExecutor() {
        return KeyAffinityExecutor.newSerializingExecutor(3, 100 * 200, "event-pool-%d");
    }

    @Bean
    @ConditionalOnMissingBean
    public TaskDataPartitionValue taskDataPartitionValue() {
        return taskType -> Optional.ofNullable(taskType).map(p -> p.name()).orElse(TaskType.FILE_EXPORT.name());
    }

    @Bean(AbstractApplicationContext.APPLICATION_EVENT_MULTICASTER_BEAN_NAME)
    @ConditionalOnMissingBean
    public TaskApplicationEventMulticaster eventMulticaster(@Qualifier("keyAffinityExecutor") KeyAffinityExecutor executorService) {
        TaskApplicationEventMulticaster eventMulticaster = new TaskApplicationEventMulticaster(executorService, e -> ExceptionUtils.wrapAndThrow(e));
        return eventMulticaster;
    }

    @Bean(TASK_FILE_EVENT_EXECUTOR_NAME)
    public KeyAffinityExecutor taskKeyAffinityExecutor() {
        return KeyAffinityExecutor.newSerializingExecutor(5, 100 * 200, "task-executor-%d");
    }

    //@ConditionalOnMissingBean(Executor.class)
    /*public ThreadPoolTaskExecutor eventTaskExecutor(ObjectProvider<TaskDecorator> taskDecorator) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100 * 200);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("event-");
        executor.setTaskDecorator(new Log4j2ThreadConfig.Log4j2TaskDecorator(taskDecorator.orderedStream().collect(Collectors.toList())));
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        return executor;
    }*/
}
