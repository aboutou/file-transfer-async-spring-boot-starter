package org.spring.file.transfer.async.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phantomthief.pool.KeyAffinityExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.input.AutoCloseInputStream;
import org.apache.commons.lang3.StringUtils;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.exception.TaskException;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.entities.model.Res;
import org.spring.file.transfer.async.domain.events.TaskChanagedEvent;
import org.spring.file.transfer.async.domain.handle.TaskErrorHandler;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import org.spring.file.transfer.async.utils.FastJsonUtil;
import org.spring.file.transfer.async.utils.SpringContextHolderUtil;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午11:52
 */
@Slf4j
public abstract class AbstractTaskHandler<T, R> implements TaskHandler<T, R> {

    @Override
    public boolean isSupported(TaskType taskType) {
        return getTaskType().equals(taskType);
    }

    public abstract TaskType getTaskType();

    @Override
    public Res<R> execute(TaskType taskType, Req<T> req) {
        TaskInstance<R> taskInstance = doExcute(req);
        if (taskInstance == null) {
            throw new RuntimeException("任务添加失败");
        }
        Res<R> res = convert(taskInstance);
        return res;
    }

    /**
     * 文件导入执行
     *
     * @param req
     * @return
     */
    public TaskInstance<R> doExcute(Req<T> req) {
        for (TaskFileService taskService : getTaskFileService()) {
            BizType bizType = taskService.getBizType();
            String bizCode = Optional.ofNullable(req).map(Req::getBizType).map(BizType::getCode).orElse(null);
            boolean biz = Optional.ofNullable(bizType).map(BizType::getCode).map(p -> StringUtils.equalsIgnoreCase(p, bizCode)).orElse(false);
            if (!biz) {
                continue;
            }
            String taskParam = Optional.ofNullable(req).map(Req::getTaskParam).map(String::valueOf).orElse(null);
            ObjectMapper objectMapper = Optional.ofNullable(getObjectMapper()).orElseGet(() -> FastJsonUtil.getObjectMapper());
            Object obj = FastJsonUtil.parseObject(taskParam, taskService.getParamClass(), objectMapper);

            taskService.preConditions(obj);

            boolean supportAsync = taskService.isSupportAsync();
            boolean async = Boolean.TRUE.equals(req.getAsync());
            TaskRepository taskRepository = taskService.getTaskRepository();
            Serializable taskId = taskService.getTaskIdGenerator().generateId(req.getTaskType());
            TaskInstance taskInstance = taskService.getTaskInstance(req);
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
            String validateParams = taskService.validateParams(obj);

            if (StringUtils.isNotBlank(validateParams)) {
                TaskChanagedEvent event = new TaskChanagedEvent(taskRepository, taskId, false, getTaskType());
                event.setTaskState(TaskState.失败);
                event.setFailReason(getTaskType().getDesc() + "参数验证失败：" + validateParams);
                event.setErrorShowType(ErrorShowType.TOAST);
                SpringContextHolderUtil.publishEvent(event);
                return taskRepository.find(getTaskType(), taskId);
            }
            if (async && supportAsync) {
                KeyAffinityExecutor executorService = taskService.getExecutorService();
                String hashKey = Optional.ofNullable(taskService.getTaskDataPartitionValue()).map(p -> p.getHashDataPartitionValue(getTaskType())).map(String::valueOf).orElse(taskService.getBizType().getCode());
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, true);
                executorService.submit(hashKey, (() -> {
                    excuteTask(obj, taskService, taskId, true);
                    return taskId;
                }));
            } else {
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, false);
                excuteTask(obj, taskService, taskId, false);
            }
            return taskRepository.find(getTaskType(), taskId);
        }
        return null;
    }

    public void excuteTask(Object param, TaskFileService taskFileService, Serializable taskId, boolean async) {
        boolean hasError = false;
        Throwable ec = null;
        try {
            ((AbstractTaskFileService) taskFileService).handleExportDataProgress(taskId, TaskState.执行中, async);
            taskFileService.handleTaskBefore(taskId, param);
            doExcuteTask(param, taskFileService, taskId, async);
            taskFileService.handleTaskSuccess(taskId, param);
        } catch (TaskException e) {
            ec = e;
            log.info(getTaskType().getDesc() + "报错：" + taskFileService.getBizType() + ", 错误信息为" + e.getMessage(), e);
            ((AbstractTaskFileService) taskFileService).handleExportDataProgress(taskId, e.getErrorShowType(), e.getFailReason(), async);
            taskFileService.handleTaskException(taskId, param, e);
        } catch (Throwable e) {
            ec = e;
            hasError = true;
            log.error(getTaskType().getDesc() + "报错：" + taskFileService.getBizType() + ", 错误信息为" + e.getMessage(), e);
            TaskErrorHandler taskErrorHandler = taskFileService.getTaskErrorHandler();
            if (taskErrorHandler != null) {
                Throwable throwable = taskErrorHandler.handleError(e, taskId, taskFileService.getBizType());
                if (throwable instanceof TaskException) {
                    ((AbstractTaskFileService) taskFileService).handleExportDataProgress(taskId, ((TaskException) throwable).getErrorShowType(), ((TaskException) throwable).getFailReason(), async);
                    hasError = false;
                }
            }
            taskFileService.handleTaskException(taskId, param, e);
        } finally {
            if (hasError) {
                ((AbstractTaskFileService) taskFileService).handleExportDataProgress(taskId, ErrorShowType.TOAST, getTaskType().getDesc() + "任务处理出错", async);
            }
            taskFileService.handleTaskComplete(taskId, param, ec);
        }
    }

    public void doExcuteTask(Object param, TaskFileService taskFileService, Serializable taskId, boolean async) {

    }

    protected Res<R> convert(TaskInstance<R> taskInstance) {
        Res<R> res = new Res<>();
        res.setTaskType(taskInstance.getTaskType());
        res.setBizType(taskInstance.getBizType());
        res.setCompletePercent(taskInstance.getCompletePercent());
        res.setFailNum(Optional.ofNullable(taskInstance.getFailNum()).orElse(0L));
        res.setFailResults(taskInstance.getFailResults());
        res.setFailReason(taskInstance.getFailReason());
        res.setSucessNum(Optional.ofNullable(taskInstance.getSucessNum()).orElse(0L));
        res.setTaskId(taskInstance.getTaskId());
        res.setTaskState(taskInstance.getTaskState());
        res.setTotalNum(Optional.ofNullable(taskInstance.getTotalNum()).orElse(0L));
        res.setCreateTime(taskInstance.getCreateTime());
        res.setUpdateTime(taskInstance.getUpdateTime());
        res.setResult(taskInstance.getResult());
        res.setAsyncFlag(taskInstance.getAsyncFlag());
        res.setWaitResultFlag(taskInstance.getWaitResultFlag());
        res.setErrorShowType(taskInstance.getErrorShowType());
        res.setMaxPollingTimes(taskInstance.getMaxPollingTimes());
        return res;
    }

    protected InputStream getInputStream(OutputStream outputStream) {
        InputStream inputStream = null;
        if (outputStream instanceof org.apache.commons.io.output.ByteArrayOutputStream) {
            org.apache.commons.io.output.ByteArrayOutputStream outputStream1 = (org.apache.commons.io.output.ByteArrayOutputStream) outputStream;
            inputStream = new AutoCloseInputStream(outputStream1.toInputStream());
        } else if (outputStream instanceof ByteArrayOutputStream) {
            ByteArrayOutputStream outputStream1 = (ByteArrayOutputStream) outputStream;
            inputStream = new AutoCloseInputStream(new ByteArrayInputStream(outputStream1.toByteArray()));
        } else if (outputStream instanceof FileOutputStream) {
            FileOutputStream fileOutputStream = (FileOutputStream) outputStream;
            FileChannel channel = fileOutputStream.getChannel();

        }
        return inputStream;
    }

    @Override
    public Res<R> find(TaskType taskType, String bizType, Serializable taskId) {
        for (TaskFileService taskService : getTaskFileService()) {
            BizType bizType1 = taskService.getBizType();
            if (!StringUtils.equalsIgnoreCase(bizType, bizType1.getCode())) {
                continue;
            }
            TaskInstance taskInstance = taskService.getTaskRepository().find(taskType, taskId);
            if (taskInstance == null) {
                return null;
            }
            return convert(taskInstance);
        }
        return null;
    }

    @Override
    public void remove(TaskType taskType, String bizType, Serializable taskId) {
        for (TaskFileService taskService : getTaskFileService()) {
            BizType bizType1 = taskService.getBizType();
            if (!StringUtils.equalsIgnoreCase(bizType, bizType1.getCode())) {
                continue;
            }
            taskService.getTaskRepository().deleteById(taskType, taskId);
        }
    }

    @Override
    public List<Res<R>> findAll() {
        List<Res<R>> resLists = new ArrayList<>();
        Set<TaskRepository> taskRepositories = getTaskFileService().stream().map(TaskFileService::getTaskRepository).filter(Objects::nonNull).collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(taskRepositories)) {
            return resLists;
        }
        taskRepositories.removeIf(p -> {
            List<TaskInstance> all = p.findByTaskType(getTaskType());
            if (CollectionUtils.isNotEmpty(all)) {
                all.removeIf(t -> {
                    resLists.add(this.convert(t));
                    return true;
                });
            }
            return true;
        });
        return resLists;
    }

    public abstract List<? extends TaskFileService> getTaskFileService();

    public ObjectMapper getObjectMapper() {
        return null;
    }
}
