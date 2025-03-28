package org.spring.file.transfer.async.domain.repository;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskInstance;

import java.io.Serializable;
import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午4:05
 */
public interface TaskRepository<T extends TaskInstance> {


    /**
     * 保存
     *
     * @param task
     */
    void save(T task);


    /**
     * 查询所有
     *
     * @return
     */
    List<T> findAll();


    /**
     * 根据任务ID查询任务
     *
     * @param taskId 任务ID
     * @param taskType 任务ID
     * @return
     */
    T find(TaskType taskType, Serializable taskId);

    /**
     * 根据任务类型查询任务
     *
     * @param taskType
     * @return
     */
    List<T> findByTaskType(TaskType taskType);

    /**
     * 根据任务ID删除任务
     *
     * @param taskType 任务ID
     * @param taskId 任务ID
     */
    void deleteById(TaskType taskType, Serializable taskId);

    /**
     * 更新
     *
     * @param taskType
     * @param taskId
     * @param task
     * @return
     */
    T update(TaskType taskType, Serializable taskId, T task);

    /**
     * 更新
     *
     * @param taskType
     * @param taskId
     * @param task
     * @return
     */
    T updateIfPresent(TaskType taskType, Serializable taskId, T task);
}
