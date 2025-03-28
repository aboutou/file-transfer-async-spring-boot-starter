package org.spring.file.transfer.async.core.export.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phantomthief.pool.KeyAffinityExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import org.spring.file.transfer.async.commons.*;
import org.spring.file.transfer.async.core.AbstractTaskFileService;
import org.spring.file.transfer.async.core.AbstractTaskHandler;
import org.spring.file.transfer.async.core.exception.TaskException;
import org.spring.file.transfer.async.core.export.service.TaskFileExportService;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.events.TaskChanagedEvent;
import org.spring.file.transfer.async.domain.events.TaskResultChanagedEvent;
import org.spring.file.transfer.async.domain.handle.TaskErrorHandler;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import org.spring.file.transfer.async.utils.ClassUtil;
import org.spring.file.transfer.async.utils.FastJsonUtil;
import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import org.springframework.http.HttpOutputMessage;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author tiny
 * 
 * @since 2023/5/15 下午2:44
 */
@Slf4j
public abstract class AbstractFileExportHandler<R> extends AbstractTaskHandler<String, R> {

    private final List<TaskFileExportService> taskFileExportServices;


    public AbstractFileExportHandler(List<TaskFileExportService> taskFileExportServices) {
        this.taskFileExportServices = taskFileExportServices;
    }

    @Override
    public TaskInstance<R> doExcute(Req<String> req) {
        for (TaskFileExportService taskService : taskFileExportServices) {
            BizType bizType = taskService.getBizType();
            String bizCode = Optional.ofNullable(req).map(Req::getBizType).map(BizType::getCode).orElse(null);
            boolean biz = Optional.ofNullable(bizType).map(BizType::getCode).map(p -> StringUtils.equalsIgnoreCase(p, bizCode)).orElse(false);
            if (!biz) {
                continue;
            }
            Class<?> paramClass = taskService.getParamClass();
            String taskParam = req.getTaskParam();
            Object obj = null;
            if (StringUtils.isNotBlank(taskParam)) {
                ObjectMapper objectMapper = Optional.ofNullable(getObjectMapper()).orElseGet(() -> FastJsonUtil.getObjectMapper());
                obj = FastJsonUtil.parseObject(taskParam, paramClass, objectMapper);
            } else {
                obj = ClassUtil.newInstance(paramClass);
            }
            if (obj == null) {
                obj = ClassUtil.newInstance(paramClass);
            }
            taskService.preConditions(obj);

            boolean supportAsync = taskService.isSupportAsync();
            boolean async = Boolean.TRUE.equals(req.getAsync());

            TaskRepository taskRepository = taskService.getTaskRepository();
            TaskInstance taskInstance = taskService.getTaskInstance(req);
            Serializable taskId = taskService.getTaskIdGenerator().generateId(req.getTaskType());
            taskInstance.setTaskId(taskId);
            taskInstance.setAsyncFlag(async && supportAsync ? 1 : 0);
            taskInstance.setWaitResultFlag(taskService.waitResult() ? 1 : 0);
            taskInstance.setErrorShowType(taskService.errorShowType());
            if (async && supportAsync && taskService.waitResult()) {
                taskInstance.setMaxPollingTimes(taskService.getMaxPollingTimes());
            }
            taskInstance.setTaskState(TaskState.新建);
            taskInstance.setBizType(new BizType.DefaultBizType(taskService.getBizType()));
            taskRepository.save(taskInstance);

            if (async && supportAsync) {
                KeyAffinityExecutor executorService = taskService.getExecutorService();
                Object finalObj = obj;
                String hashKey = Optional.ofNullable(taskService.getTaskDataPartitionValue()).map(p -> p.getHashDataPartitionValue(getTaskType())).map(String::valueOf).orElse(taskService.getBizType().getCode());
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, async);
                executorService.submit(hashKey, (() -> {
                    excuteExport(finalObj, taskService, taskId, true);
                    return taskId;
                }));
            } else {
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, async);
                excuteExport(obj, taskService, taskId, false);
            }
            return taskRepository.find(req.getTaskType(), taskId);
        }
        return null;
    }


    public void excuteExport(Object param, TaskFileExportService taskService, Serializable taskId, boolean async) {
        boolean hasError = false;
        Throwable ec = null;
        try {
            ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.执行中, async);
            taskService.handleTaskBefore(taskId, param);
            excuteExport0(param, taskService, taskId, async);
            taskService.handleTaskSuccess(taskId, param);
        } catch (TaskException e) {
            ec = e;
            log.info("导出文件报错：" + taskService.getBizType() + "， 错误信息为" + e.getMessage(), e);
            // taskService.han
            ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, e.getErrorShowType(), e.getFailReason(), async);
            taskService.handleTaskException(taskId, param, e);
        } catch (Throwable e) {
            hasError = true;
            ec = e;
            log.error("导出文件报错：" + taskService.getBizType() + "， 错误信息为" + e.getMessage(), e);
            TaskErrorHandler taskErrorHandler = taskService.getTaskErrorHandler();
            if (taskErrorHandler != null) {
                Throwable throwable = taskErrorHandler.handleError(e, taskId, taskService.getBizType());
                if (throwable instanceof TaskException) {
                    ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, ((TaskException) throwable).getErrorShowType(), ((TaskException) throwable).getFailReason(), async);
                    hasError = false;
                }
            }
            taskService.handleTaskException(taskId, param, e);
        } finally {
            if (hasError) {
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, ErrorShowType.TOAST, "导出文件任务处理出错", async);
            }
            taskService.handleTaskComplete(taskId, param, ec);
        }
    }

    public void excuteExport0(Object param, TaskFileExportService taskService, Serializable taskId, boolean async) {
        // 数据验证
        String validate = taskService.validateParams(param);
        TaskRepository taskRepository = taskService.getTaskRepository();
        {
            TaskChanagedEvent event = new TaskChanagedEvent(taskRepository, taskId, async, getTaskType());
            if (StringUtils.isNotBlank(validate)) {
                event.setTaskState(TaskState.失败);
                event.setFailReason("参数失败：" + validate);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
        }

        org.apache.commons.io.output.ByteArrayOutputStream outputStream = new org.apache.commons.io.output.ByteArrayOutputStream();
        HttpOutputMessage httpOutputMessage = taskService.getHttpOutputMessage(outputStream);
//        String fileName = taskService.fileName();
        String fileName = StringUtils.isNotBlank(taskService.fileName()) ? taskService.fileName() : buildFileName(taskService.getBizType().getName());

        String fileNameExt = fileName + "." + StringUtils.lowerCase(taskService.fileFormat().getFileExtensionName());

        httpOutputMessage.getHeaders().add(TaskFileExportService.FILE_NAME_HEADER, fileNameExt);
        httpOutputMessage.getHeaders().add(TaskFileExportService.FILE_EXTENSION_NAME_HEADER, taskService.fileFormat().getFileExtensionName());
        ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.数据获取中, async);
        taskService.executeExport(taskId, param, httpOutputMessage);
        ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.文件生成中, async);
        R r = excuteFileHandle(taskId, taskService.fileContentType(), httpOutputMessage);
        // TaskInstance<R> taskInstance = taskRepository.find(TaskType.FILE_EXPORT, taskId);
        // taskInstance.setResult(r);
        if (Objects.isNull(r)) {
            throw new TaskException(ErrorShowType.TOAST, "无法获取文件处理结果");
        }

        TaskResultChanagedEvent event = new TaskResultChanagedEvent(taskRepository, taskId, async, getTaskType());
        event.setTaskState(TaskState.执行完成);
        event.setResult(r);
        SpringContextHolderUtil.publishEvent(event);
        //taskRepository.update(TaskType.FILE_EXPORT, taskId, taskInstance);
    }

    private String buildFileName(String bizTypeName) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        return StringUtils.join(bizTypeName, "_", LocalDateTime.now().format(formatter), RandomStringUtils.randomNumeric(4));
    }

    /**
     * 返回结果
     *
     * @param taskId
     * @param httpOutputMessage
     * @return
     */
    public R excuteFileHandle(Serializable taskId, FileContentType fileContentType, HttpOutputMessage httpOutputMessage) {
        return excuteFileHandle(taskId, httpOutputMessage);
    }

    /**
     * 返回结果
     *
     * @param taskId
     * @param httpOutputMessage
     * @return
     */
    public R excuteFileHandle(Serializable taskId, HttpOutputMessage httpOutputMessage) {
        return null;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.FILE_EXPORT;
    }

    @Override
    public List<TaskFileExportService> getTaskFileService() {
        return taskFileExportServices;
    }

}
