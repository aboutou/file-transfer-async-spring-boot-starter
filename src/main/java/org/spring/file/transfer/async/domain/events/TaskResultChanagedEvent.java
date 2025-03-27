package org.spring.file.transfer.async.domain.events;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Setter
@Getter
@ToString
public class TaskResultChanagedEvent<R> extends TaskChanagedEvent {

    public TaskResultChanagedEvent(TaskRepository taskRepository, Serializable taskId, boolean async, TaskType taskType) {
        super(taskRepository, taskId, async, taskType);
    }

    private R result;
}
