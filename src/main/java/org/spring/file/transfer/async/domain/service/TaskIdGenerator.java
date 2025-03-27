package org.spring.file.transfer.async.domain.service;

import org.spring.file.transfer.async.commons.TaskType;

import java.io.Serializable;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午4:45
 */
public interface TaskIdGenerator {


    /**
     * 生成任务ID
     * @param taskType
     * @return
     */
    Serializable generateId(TaskType taskType);
}
