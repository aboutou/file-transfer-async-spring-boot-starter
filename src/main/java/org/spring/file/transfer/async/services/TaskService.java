package org.spring.file.transfer.async.services;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.entities.model.Res;

import java.io.Serializable;
import java.util.List;

/**
 * @author bm
 */
public interface TaskService<T, R> {


    /**
     * 任务执行
     *
     * @param req
     * @return
     */
    Res<R> execute(Req<T> req);

    /**
     * 查询
     *
     * @param taskType
     * @param taskId
     * @return
     */
    Res<R> find(TaskType taskType, String bizType, Serializable taskId);


    /**
     * 移除任务
     *
     * @param taskType
     * @param taskId
     */
    void remove(TaskType taskType, String bizType, Serializable taskId);

    /**
     * 查询所有任务
     *
     * @return
     */
    List<Res<R>> findAll();
}
