package org.spring.file.transfer.async.domain.repository.impl;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.repository.TaskRepository;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/17 下午5:11
 */
public class ConcurrentMapTaskInstanceRepositoryImpl implements TaskRepository<TaskInstance> {


    private final ConcurrentMap<TaskType, ConcurrentMap<Serializable, TaskInstance>> taskInstanceConcurrentMap;


    public ConcurrentMapTaskInstanceRepositoryImpl() {
        this.taskInstanceConcurrentMap = new ConcurrentHashMap<>();
    }

    @Override
    public void save(TaskInstance task) {
        TaskType taskType = task.getTaskType();
        ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(taskType, new ConcurrentHashMap<>());
        map.put(task.getTaskId(), task);
        taskInstanceConcurrentMap.put(taskType, map);
    }

    @Override
    public List<TaskInstance> findAll() {
        return Optional.ofNullable(taskInstanceConcurrentMap).map(Map::values).map(Collection::stream).map(p -> p.map(p1 -> p1.values()).flatMap(Collection::stream).collect(Collectors.toList())).orElseGet(ArrayList::new);
    }

    @Override
    public TaskInstance find(TaskType taskType, Serializable taskId) {
        ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(taskType, new ConcurrentHashMap<>());
        TaskInstance taskInstance = map.get(taskId);
        if (taskInstance != null) {
            return taskInstance;
        }
        return null;
    }

    @Override
    public List<TaskInstance> findByTaskType(TaskType taskType) {
        ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(taskType, new ConcurrentHashMap<>());
        return new ArrayList<>(map.values());
    }

    @Override
    public void deleteById(TaskType taskType, Serializable taskId) {
        for (TaskType value : TaskType.values()) {
            ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(value, new ConcurrentHashMap<>());
            TaskInstance taskInstance = map.get(taskId);
            if (taskInstance != null) {
                map.remove(taskId);
                return;
            }
        }
    }

    @Override
    public TaskInstance update(TaskType taskType, Serializable taskId, TaskInstance task) {
        ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(taskType, new ConcurrentHashMap<>());
        if (map.get(taskId) != null) {
            map.put(taskId, task);
            return task;
        }
        return null;
    }

    @Override
    public TaskInstance updateIfPresent(TaskType taskType, Serializable taskId, TaskInstance task) {
        ConcurrentMap<Serializable, TaskInstance> map = taskInstanceConcurrentMap.getOrDefault(taskType, new ConcurrentHashMap<>());
        return map.putIfAbsent(taskId, task);
    }
}
