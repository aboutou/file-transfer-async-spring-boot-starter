package org.spring.file.transfer.async.domain.service.impl;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.service.TaskIdGenerator;
import org.apache.commons.lang3.RandomStringUtils;

/**
 * @author tiny
 * 
 * @since 2023/5/17 下午6:15
 */
public class DefaultTaskIdGenerator implements TaskIdGenerator {


    @Override
    public String generateId(TaskType taskType) {
        return RandomStringUtils.randomNumeric(8);
    }
}
