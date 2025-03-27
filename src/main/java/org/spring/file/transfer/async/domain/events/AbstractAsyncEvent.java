package org.spring.file.transfer.async.domain.events;

import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * @author bm
 */
@Getter
public abstract class AbstractAsyncEvent extends ApplicationEvent {

    private final boolean async;


    public AbstractAsyncEvent(boolean async) {
        super(SpringContextHolderUtil.getApplicationContext());
        this.async = async;
    }

}
