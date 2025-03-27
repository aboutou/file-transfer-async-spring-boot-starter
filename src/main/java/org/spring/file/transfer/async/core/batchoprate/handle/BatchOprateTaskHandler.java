package org.spring.file.transfer.async.core.batchoprate.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.FailResult;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.AbstractTaskFileService;
import org.spring.file.transfer.async.core.AbstractTaskHandler;
import org.spring.file.transfer.async.core.TaskFileService;
import org.spring.file.transfer.async.core.batchoprate.service.BatchOprateTaskService;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.events.TaskChanagedEvent;
import org.spring.file.transfer.async.domain.events.TaskResultChanagedEvent;
import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author wuzhencheng
 */
@Slf4j
public class BatchOprateTaskHandler<P> extends AbstractTaskHandler<String, String> {


    private final List<BatchOprateTaskService> batchOprateTaskServices;
    private ObjectMapper objectMapper;

    public BatchOprateTaskHandler(List<BatchOprateTaskService> batchOprateTaskServices) {
        this.batchOprateTaskServices = batchOprateTaskServices;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.BATCH_OPERATION;
    }


    @Override
    public void doExcuteTask(Object param, TaskFileService taskService, Serializable taskId, boolean async) {
        BatchOprateTaskService batchOprateTaskService = (BatchOprateTaskService) taskService;
        long totalNum = batchOprateTaskService.totalNum(param);
        if (totalNum == 0) {
            ((AbstractTaskFileService) batchOprateTaskService).handleExportDataProgress(taskId, TaskState.执行完成, async);
            return;
        }
        ((AbstractTaskFileService) batchOprateTaskService).handleExportDataProgress(taskId, totalNum, 0L, 0L);
        // 验证数据
        List<FailResult> validate = batchOprateTaskService.validate(param);
        int failSize = CollectionUtils.size(validate);
        if (CollectionUtils.isNotEmpty(validate)) {
            TaskChanagedEvent event = new TaskChanagedEvent(batchOprateTaskService.getTaskRepository(), taskId, async, getTaskType());
            handleFailLists(totalNum, validate, event);
            SpringContextHolderUtil.publishEvent(event);
            if (totalNum == failSize) {
                return;
            }
        }
        // 处理数据
        List<FailResult> results = batchOprateTaskService.dataHandle(taskId, param);
        TaskResultChanagedEvent event = new TaskResultChanagedEvent(batchOprateTaskService.getTaskRepository(), taskId, async, getTaskType());
        //表示有错误
        if (CollectionUtils.isNotEmpty(results)) {
            handleFailLists(0, results, event);
        }
        event.setTaskState(TaskState.执行完成);
        SpringContextHolderUtil.publishEvent(event);
    }

    private void handleFailLists(long totalNum, List<FailResult> failLists, TaskChanagedEvent event) {
        int failSize = failLists.size();
        List<TaskFailResultInstance> failResults = failLists.stream().map(p -> new TaskFailResultInstance(p)).collect(Collectors.toList());
        event.setFailNum((long) failSize);
        // 返回的数据异常的情况
        if (totalNum == failSize) {
            event.setTaskState(TaskState.失败);
            event.setFailReason("所有数据都不符合条件");
        }
        event.setFailResults(failResults);
        event.setErrorShowType(ErrorShowType.CSV);
    }


    @Override
    public List<? extends BatchOprateTaskService> getTaskFileService() {
        return batchOprateTaskServices;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
