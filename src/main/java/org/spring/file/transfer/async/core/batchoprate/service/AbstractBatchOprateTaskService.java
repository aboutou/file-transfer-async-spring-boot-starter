package org.spring.file.transfer.async.core.batchoprate.service;

import org.spring.file.transfer.async.commons.FailResult;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.AbstractTaskFileService;

/**
 * @author wuzhencheng
 */
public abstract class AbstractBatchOprateTaskService<R extends FailResult<String>, P> extends AbstractTaskFileService<P> implements BatchOprateTaskService<R, P> {

    @Override
    public TaskType getTaskType() {
        return TaskType.BATCH_OPERATION;
    }
}
