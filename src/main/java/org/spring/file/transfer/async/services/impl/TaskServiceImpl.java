package org.spring.file.transfer.async.services.impl;


import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.TaskHandler;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.entities.model.Res;
import org.spring.file.transfer.async.services.TaskService;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午11:39
 */
@AllArgsConstructor
public class TaskServiceImpl implements TaskService<String, String> {


    private final List<TaskHandler> taskHandlers;

    @Override
    public Res<String> execute(Req<String> req) {
        for (TaskHandler taskHandler : taskHandlers) {
            if (taskHandler.isSupported(req.getTaskType())) {
                return taskHandler.execute(req.getTaskType(), req);
            }
        }
        throw new RuntimeException("未找到【" + req.getTaskType() + "】的实现类");
    }

    @Override
    public Res<String> find(TaskType taskType, String bizType, Serializable taskId) {
        if (taskId == null || StringUtils.isAnyBlank(bizType, String.valueOf(taskId))) {
            return null;
        }
        for (TaskHandler taskHandler : taskHandlers) {
            if (taskHandler.isSupported(taskType)) {
                return taskHandler.find(taskType, bizType, taskId);
            }
        }
        return null;
    }

    @Override
    public void remove(TaskType taskType, String bizType, Serializable taskId) {
        if (taskId == null || StringUtils.isAnyBlank(bizType, String.valueOf(taskId))) {
            return;
        }
        for (TaskHandler taskHandler : taskHandlers) {
            if (taskHandler.isSupported(taskType)) {
                taskHandler.remove(taskType, bizType, taskId);
                return;
            }
        }
    }

    @Override
    public List<Res<String>> findAll() {
        TaskType[] values = TaskType.values();
        List<Res<String>> resLists = new ArrayList<>();
        for (TaskType taskType : values) {
            for (TaskHandler taskHandler : taskHandlers) {
                if (taskHandler.isSupported(taskType)) {
                    List<Res<String>> all = taskHandler.findAll();
                    if (CollectionUtils.isNotEmpty(all)) {
                        resLists.addAll(all);
                    }
                }
            }
        }
        Comparator<Res<String>> comparator = Comparator.comparing(Res::getCreateTime, LocalDateTime::compareTo);
        return resLists.stream().distinct().sorted(comparator.reversed()).collect(Collectors.toList());
    }
}
