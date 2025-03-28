package org.spring.file.transfer.async.core;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.entities.model.Res;

import java.io.Serializable;
import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/8 下午3:28
 */
public interface TaskHandler<T, R> {

    /**
     * 支持的任务类型
     *
     * @param taskType
     * @return
     */
    boolean isSupported(TaskType taskType);


    Res<R> execute(TaskType taskType, Req<T> req);


    Res<R> find(TaskType taskType, String bizType, Serializable taskId);


    void remove(TaskType taskType, String bizType, Serializable taskId);

    List<Res<R>> findAll();
}
