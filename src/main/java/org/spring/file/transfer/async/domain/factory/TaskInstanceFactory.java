package org.spring.file.transfer.async.domain.factory;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午4:52
 */
public class TaskInstanceFactory {


    public TaskInstance createTaskInstance(Req req) {
        TaskType taskType = req.getTaskType();
        switch (taskType) {
            case FILE_IMPORT:
            case BATCH_OPERATION:
            case FILE_EXPORT:
        }
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setTaskType(req.getTaskType());
        taskInstance.setBizType(req.getBizType());
        taskInstance.setCreateTime(LocalDateTime.now());
        taskInstance.setUpdateTime(LocalDateTime.now());
        taskInstance.setCompletePercent(BigDecimal.ZERO);
        // TaskInstanceAssembler assembler = Mappers.getMapper(TaskInstanceAssembler.class);
        return taskInstance;
    }
}
