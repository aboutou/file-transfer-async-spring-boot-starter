package org.spring.file.transfer.async.domain.events;

import java.io.Serializable;

/**
 * @author bm
 */
public abstract class AbstractAsyncHashKeyEvent extends AbstractAsyncEvent {


    public AbstractAsyncHashKeyEvent(boolean async) {
        super(async);
    }


    /**
     * 同样的类型任务需要在同一队列进行执行
     *
     * @return
     */
    public abstract Serializable getHashKey();
}
