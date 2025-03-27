package org.spring.file.transfer.async.core.batchoprate.service;

import org.spring.file.transfer.async.commons.FailResult;
import org.spring.file.transfer.async.core.TaskFileService;

import java.io.Serializable;
import java.util.List;

/**
 * @author wuzhencheng
 */
public interface BatchOprateTaskService<R extends FailResult, P> extends TaskFileService<P> {



    long totalNum(P params);

    /**
     * 数据校验
     *
     * @param param
     * @return
     */
    default List<R> validate(P param) {
        return null;
    }


    /**
     * 数据处理
     *
     * @param taskId
     * @param param
     * @return
     */
    List<R> dataHandle(Serializable taskId, P param);
}
