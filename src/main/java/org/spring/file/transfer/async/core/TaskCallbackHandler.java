package org.spring.file.transfer.async.core;

import java.io.Serializable;

/**
 * @author wuzhencheng
 */
public interface TaskCallbackHandler<P> {


    /**
     * 消息任务之前
     *
     * @param taskId
     * @param param
     */
    default void handleTaskBefore(final Serializable taskId, final P param) {
    }

    /**
     * 消息任务异常
     *
     * @param taskId
     * @param param
     * @param e
     */
    default void handleTaskException(final Serializable taskId, final P param, final Throwable e) {
    }

    /**
     * 消息任务成功
     *
     * @param taskId
     * @param param
     */
    default void handleTaskSuccess(final Serializable taskId, final P param) {
    }

    /**
     * 消息任务完成
     *
     * @param taskId
     * @param param
     * @param e
     */
    default void handleTaskComplete(final Serializable taskId, final P param, final Throwable e) {
    }

}
