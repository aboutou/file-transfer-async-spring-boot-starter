package org.spring.file.transfer.async.core;

import org.spring.file.transfer.async.commons.TaskType;

/**
 * 根据任务获取任务分割值
 *
 * @author wuzhencheng
 */
@FunctionalInterface
public interface TaskDataPartitionValue<R> {


    /**
     * 获取任务分割值
     *
     * @param taskType
     * @return
     */
    R getHashDataPartitionValue(TaskType taskType);
}
