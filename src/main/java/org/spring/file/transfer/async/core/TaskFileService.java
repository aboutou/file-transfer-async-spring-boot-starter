package org.spring.file.transfer.async.core;

import com.github.phantomthief.pool.KeyAffinityExecutor;
import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.handle.TaskErrorHandler;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import org.spring.file.transfer.async.domain.service.TaskIdGenerator;

/**
 * @author tiny
 * 
 * @since 2023/5/14 下午10:06
 */
public interface TaskFileService<P> extends TaskCallbackHandler<P> {

    /**
     * 业务类型
     *
     * @return
     */
    BizType getBizType();

    /**
     * 是否支持异步
     *
     * @return
     */
    boolean isSupportAsync();

    /**
     * 任务的前置校验，比如任务不能同时进行多
     *
     * @param param
     */
    default void preConditions(P param) {

    }


    /**
     * 数据存储仓储
     *
     * @return
     */
    TaskRepository getTaskRepository();

    /**
     * ID生成器
     *
     * @return
     */
    TaskIdGenerator getTaskIdGenerator();

    /**
     * 任务生成工厂
     *
     * @return
     */
    TaskInstance getTaskInstance(Req<P> req);

    /**
     * 线程池
     *
     * @return
     */
    KeyAffinityExecutor getExecutorService();

    /**
     * 错误处理
     *
     * @return
     */
    TaskErrorHandler getTaskErrorHandler();

    /**
     * 错误分割处理
     *
     * @return
     */
    TaskDataPartitionValue getTaskDataPartitionValue();

    /**
     * 请求的参数对象
     *
     * @return
     */
    Class<P> getParamClass();

    /**
     * 参数校验
     *
     * @param params
     * @return String
     */
    String validateParams(P params);

    /**
     * 是否需要等待结果，为了兼容那种文件中心和任务列表的情况
     *
     * @return
     */
    boolean waitResult();

    /**
     * 错误提示方式
     *
     * @return
     */
    default ErrorShowType errorShowType() {
        return ErrorShowType.CSV;
    }

    /**
     * 最结果的最大轮询次数，当且是异步和需要等待结果的时候才生效
     *
     * @return
     */
    default Integer getMaxPollingTimes() {
        return 180;
    }
}
