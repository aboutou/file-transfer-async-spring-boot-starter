package org.spring.file.transfer.async.core;

import com.github.phantomthief.pool.KeyAffinityExecutor;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.config.EventConfig;
import org.spring.file.transfer.async.config.TaskAutoConfig;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.events.TaskChanagedEvent;
import org.spring.file.transfer.async.domain.factory.TaskInstanceFactory;
import org.spring.file.transfer.async.domain.handle.TaskErrorHandler;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import org.spring.file.transfer.async.domain.service.TaskIdGenerator;
import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 一、Spring boot运行时，会发送以下事件
 * <p>
 * 1. ApplicationStartingEvent
 * <p>
 * 2. ApplicationEnvironmentPreparedEvent：当Environment已经准备好，在context 创建前
 * <p>
 * 3. ApplicationContextInitializedEvent：在ApplicationContext 创建和ApplicationContextInitializer都被调用后，但是bean definition没有被加载前
 * <p>
 * 4. ApplicationPreparedEvent：bean definition已经加载，但是没有refresh
 * <p>
 * 5. ApplicationStartedEvent： context 已经被refresh， 但是application 和command-line 的runner都没有被调用
 * <p>
 * 6. AvailabilityChangeEvent
 * <p>
 * 7. ApplicationReadyEvent： application 和command-line 的runner都被调用后触发
 * <p>
 * 8. AvailabilityChangeEvent
 * <p>
 * 9. ApplicationFailedEvent： 启动失败触发
 *
 *
 * </p>
 *
 * @author bm
 */
@Slf4j
public abstract class AbstractTaskFileService<P> implements TaskFileService<P>, ApplicationListener<ApplicationStartedEvent> {

    protected List<FileConverter<?>> fileConverters;
    protected Validator validator;
    protected ApplicationContext applicationContext;
    private TaskRepository taskRepository;
    private KeyAffinityExecutor executorService;
    private TaskIdGenerator taskIdGenerator;
    private TaskInstanceFactory taskInstanceFactory;
    private I18nHandler i18nHandler;
    private TaskDataPartitionValue taskDataPartitionValue;


    @Override
    public boolean isSupportAsync() {
        return true;
    }

    @Override
    public TaskRepository getTaskRepository() {
        return this.taskRepository;
    }

    @Override
    public TaskIdGenerator getTaskIdGenerator() {
        return taskIdGenerator;
    }

    @Override
    public TaskInstance getTaskInstance(Req<P> req) {
        return taskInstanceFactory.createTaskInstance(req);
    }

    @Override
    public KeyAffinityExecutor getExecutorService() {
        return this.executorService;
    }

    @Override
    public TaskDataPartitionValue getTaskDataPartitionValue() {
        return this.taskDataPartitionValue;
    }

    @Override
    public TaskErrorHandler getTaskErrorHandler() {
        return new TaskErrorHandler.DefaultTaskErrorHandler();
    }


    @Override
    public String validateParams(P params) {
        return jsr303Validate(params);
    }

    protected String jsr303Validate(Object entity, Class<?>... validateGroups) {
        if (validator == null || entity == null) {
            return null;
        }
        if (i18nHandler == null) {
            i18nHandler = n -> n;
        }
        Set<ConstraintViolation<Object>> errors = validator.validate(entity, validateGroups);
        if (!CollectionUtils.isEmpty(errors)) {
            String errMsg = errors
                    .stream()
                    .map(c -> StringUtils.isBlank(c.getMessage()) ? null : c.getMessage())
                    .map(i18nHandler::getLocaleName)
                    .distinct()
                    .collect(Collectors.joining(";"));
            return errMsg;
        }
        return null;
    }

    @Override
    public boolean waitResult() {
        return true;
    }

    @Override
    public final void onApplicationEvent(ApplicationStartedEvent event) {
        log.debug("开始初始化{}实现类-> {}, 业务类型-> {}:{}", getTaskType().getDesc(), this.getClass().getSimpleName(), getBizType().getCode(), getBizType().getName());
        ApplicationContext applicationContext = event.getApplicationContext();
        this.applicationContext = applicationContext;
        this.taskRepository = applicationContext.getBean(TaskAutoConfig.TASK_FILE_TASK_REPOSITORY_NAME, TaskRepository.class);
        this.executorService = applicationContext.getBean(EventConfig.TASK_FILE_EVENT_EXECUTOR_NAME, KeyAffinityExecutor.class);
        this.taskIdGenerator = applicationContext.getBean(TaskAutoConfig.TASK_ID_GENERATOR_NAME, TaskIdGenerator.class);
        this.validator = applicationContext.getBean(Validator.class);
        this.taskInstanceFactory = applicationContext.getBean(TaskInstanceFactory.class);
        this.i18nHandler = applicationContext.getBean(I18nHandler.class);
        this.taskDataPartitionValue = applicationContext.getBean(TaskDataPartitionValue.class);
        Map<String, FileConverter> beansOfType = applicationContext.getBeansOfType(FileConverter.class);
        if (beansOfType != null) {
            List<FileConverter> collect = beansOfType.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.toList());
            AnnotationAwareOrderComparator.sort(collect);
            fileConverters = new ArrayList<>();
            for (FileConverter fileConverter : collect) {
                fileConverters.add(fileConverter);
            }
        }
        init(applicationContext);
        log.debug("结束初始化{}实现类-> {}, 业务类型-> {}", getTaskType().getDesc(), this.getClass().getSimpleName(), getBizType());
    }

    protected void init(ApplicationContext applicationContext) {

    }

    /**
     * 推送数据进度条数
     *
     * @param taskId
     * @param totalNum
     * @param successNum
     * @param failureNum
     */
    public void handleExportDataProgress(Serializable taskId, long totalNum, long successNum, long failureNum) {
        handleExportDataProgress(taskId, totalNum, successNum, failureNum, null, null, null, false);
    }

    /**
     * 推送状态
     *
     * @param taskId
     * @param taskState
     */
    public void handleExportDataProgress(Serializable taskId, TaskState taskState, boolean async) {
        handleExportDataProgress(taskId, 0, 0, 0, taskState, null, null, async);
    }

    /**
     * 推送数据进度条数
     *
     * @param taskId
     * @param errorShowType
     * @param failReason
     */
    public void handleExportDataProgress(Serializable taskId, ErrorShowType errorShowType, String failReason, boolean async) {
        handleExportDataProgress(taskId, 0, 0, 0, TaskState.失败, errorShowType, failReason, async);
    }

    /**
     * 推送数据进度条数
     *
     * @param taskId
     * @param errorShowType
     * @param failReason
     */
    public void handleExportDataProgress(Serializable taskId, ErrorShowType errorShowType, String failReason) {
        handleExportDataProgress(taskId, 0, 0, 0, TaskState.失败, errorShowType, failReason, false);
    }

    /**
     * 推送数据进度条数
     *
     * @param taskId
     * @param totalNum
     * @param successNum
     * @param failureNum
     * @param errorShowType
     */
    public void handleExportDataProgress(Serializable taskId,
                                         long totalNum,
                                         long successNum,
                                         long failureNum,
                                         TaskState taskState,
                                         ErrorShowType errorShowType,
                                         String failReason,
                                         boolean async) {
        TaskRepository taskRepository = getTaskRepository();
        TaskChanagedEvent event = new TaskChanagedEvent(taskRepository, taskId, async, getTaskType());
        event.setTotalNum(totalNum);
        event.setFailNum(failureNum);
        event.setSucessNum(successNum);
        event.setTaskState(taskState);
        event.setErrorShowType(errorShowType);
        event.setFailReason(failReason);
        SpringContextHolderUtil.publishEvent(event);
        return;
    }


    protected abstract TaskType getTaskType();


}
