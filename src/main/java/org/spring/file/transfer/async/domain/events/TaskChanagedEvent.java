package org.spring.file.transfer.async.domain.events;

import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午5:00
 */
@Setter
@Getter
@ToString
public class TaskChanagedEvent extends AbstractAsyncTaskEvent {

    public TaskChanagedEvent(TaskRepository taskRepository, Serializable taskId, boolean async, TaskType taskType) {
        super(taskRepository, taskId, async, taskType);
    }

    /**
     * 任务状态
     */
    private TaskState taskState;
    /**
     * 任务失败的原因，文件解析错误，系统报错等
     */
    private String failReason;

    /**
     * 总条数
     */
    private Long totalNum;

    /**
     * 成功条数
     */
    private Long sucessNum;

    /**
     * 全量成功数
     */
    private boolean fullSucessNum = false;

    /**
     * 失败条数
     */
    private Long failNum;

    /**
     * 错误展示方式
     */
    private ErrorShowType errorShowType;

    /**
     * 失败的数据
     */
    private List<TaskFailResultInstance> failResults;


}
