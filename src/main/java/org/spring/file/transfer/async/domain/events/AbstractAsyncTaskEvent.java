package org.spring.file.transfer.async.domain.events;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import lombok.Getter;

import java.io.Serializable;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午9:52
 */
@Getter
public abstract class AbstractAsyncTaskEvent extends AbstractAsyncHashKeyEvent {

    private final TaskRepository taskRepository;

    private final Serializable taskId;

    private final TaskType taskType;

    public AbstractAsyncTaskEvent(TaskRepository taskRepository, Serializable taskId, boolean async, TaskType taskType) {
        super(async);
        this.taskRepository = taskRepository;
        this.taskId = taskId;
        this.taskType = taskType;
    }

    @Override
    public Serializable getHashKey() {
        return getTaskId();
    }
}
