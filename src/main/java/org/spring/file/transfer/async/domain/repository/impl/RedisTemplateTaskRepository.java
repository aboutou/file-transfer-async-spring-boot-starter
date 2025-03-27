package org.spring.file.transfer.async.domain.repository.impl;

import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import com.xkzhangsan.time.converter.DateTimeConverterUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;

import java.io.Serializable;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * @author bm
 */
public class RedisTemplateTaskRepository implements TaskRepository<TaskInstance> {


    private final static String REDIS_TASK_KEY = "TASK:%s:REDIS:%d";

    private final static String REDIS_TASK_ALL_KEY = "TASK:%s:REDIS";

    @Autowired
    private RedisTemplate<String, Object> template;

    @Value("#{T(org.springframework.boot.convert.DurationStyle).detectAndParse('${spring.async.data.timeout:24h}')}")
    private Duration durationTimeOut;


    @Override
    public void save(TaskInstance task) {
        String key = getKey(task.getTaskType());
        HashOperations<String, Object, Object> hash = template.opsForHash();
        hash.put(key, task.getTaskId(), task);
        hash.getOperations().expire(key, durationTimeOut);

        String key1 = String.format(REDIS_TASK_ALL_KEY, task.getTaskType());
        SetOperations<String, Object> set = template.opsForSet();
        set.add(key1, String.valueOf(getDataPartitionValue()));
        set.getOperations().expire(key1, 30, TimeUnit.DAYS);
    }

    @Override
    public List<TaskInstance> findAll() {
        HashOperations<String, Object, Object> hash = template.opsForHash();
        List<Map<Object, Object>> lists = Stream.of(TaskType.values()).map(p -> getKey(p)).map(hash::entries).filter(Objects::nonNull).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(lists)) {
            return null;
        }
        List<TaskInstance> tasks = getTaskInstances(lists);
        return tasks;
    }

    private List<TaskInstance> getTaskInstances(List<Map<Object, Object>> lists) {
        List<TaskInstance> tasks = lists.stream().map(Map::values).flatMap(Collection::stream).filter(Objects::nonNull).map(p -> (TaskInstance) p).collect(Collectors.toList());
        return getInstances(tasks);
    }

    private List<TaskInstance> getInstances(List<TaskInstance> tasks) {
        if (CollectionUtils.isNotEmpty(tasks)) {
            tasks.removeIf(Objects::isNull);
        }
        long l = System.currentTimeMillis();
        // 处理过期的数据
        if (CollectionUtils.isNotEmpty(tasks)) {
            long timeOutMillis = durationTimeOut.toMillis();
            List<TaskInstance> taskInstances = tasks.stream()
                    .filter(p -> Objects.nonNull(p.getCreateTime()))
                    .filter(p -> (l - DateTimeConverterUtil.toEpochMilli(p.getCreateTime())) >= timeOutMillis)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            removeTaskInstance(tasks, taskInstances);
        }
        // 处理成功的数据
        if (CollectionUtils.isNotEmpty(tasks)) {
            long millis = TimeUnit.HOURS.toMillis(12);
            List<TaskInstance> compcompleteTask = tasks.stream()
                    .filter(p -> TaskState.执行完成.equals(p.getTaskState()))
                    .filter(p -> (l - DateTimeConverterUtil.toEpochMilli(p.getUpdateTime())) >= millis)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            removeTaskInstance(tasks, compcompleteTask);
        }
        return tasks;
    }

    private void removeTaskInstance(List<TaskInstance> tasks, List<TaskInstance> taskInstances) {
        if (CollectionUtils.isEmpty(taskInstances) || CollectionUtils.isEmpty(tasks)) {
            return;
        }
        taskInstances.forEach(p -> deleteById(p.getTaskType(), p.getTaskId()));
        List<Serializable> co = taskInstances.stream().map(TaskInstance::getTaskId).filter(Objects::nonNull).collect(Collectors.toList());
        tasks.removeIf(p -> co.contains(p.getTaskId()));
    }

    @Override
    public TaskInstance find(TaskType taskType, Serializable taskId) {
        String key = getKey(taskType);
        HashOperations<String, Object, Object> hash = template.opsForHash();
        return (TaskInstance) hash.get(key, taskId);
    }

    @Override
    public List<TaskInstance> findByTaskType(TaskType taskType) {
        String key = getKey(taskType);
        HashOperations<String, Object, Object> hash = template.opsForHash();
        List<TaskInstance> list = new ArrayList<>();
        List<Object> values = hash.values(key);
        if (!CollectionUtils.isEmpty(values)) {
            for (Object value : values) {
                list.add((TaskInstance) value);
            }
        }
        return getInstances(list);
    }

    @Override
    public void deleteById(TaskType taskType, Serializable taskId) {
        HashOperations<String, Object, Object> hash = template.opsForHash();
        String key = getKey(taskType);
        hash.delete(key, taskId);
    }

    @Override
    public TaskInstance update(TaskType taskType, Serializable taskId, TaskInstance task) {
        {
            HashOperations<String, Object, Object> hash = template.opsForHash();
            String key = getKey(taskType);
            hash.put(key, taskId, task);
            hash.getOperations().expire(key, durationTimeOut);
        }
        {
            String key1 = String.format(REDIS_TASK_ALL_KEY, taskType);
            template.expire(key1, 30, TimeUnit.DAYS);
        }
        return task;
    }

    @Override
    public TaskInstance updateIfPresent(TaskType taskType, Serializable taskId, TaskInstance task) {
        {
            HashOperations<String, Object, Object> hash = template.opsForHash();
            String key = getKey(taskType);
            Boolean b = hash.putIfAbsent(key, taskId, task);
            if (b) {
                hash.getOperations().expire(key, durationTimeOut);
            }
        }
        {
            String key1 = String.format(REDIS_TASK_ALL_KEY, taskType);
            template.expire(key1, 30, TimeUnit.DAYS);
        }
        return task;
    }

    private String getKey(TaskType taskType) {
        String key = String.format(REDIS_TASK_KEY, taskType.name(), getDataPartitionValue());
        return key;
    }

    protected long getDataPartitionValue() {
        return -1000L;
    }
}
