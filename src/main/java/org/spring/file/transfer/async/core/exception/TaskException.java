package org.spring.file.transfer.async.core.exception;

import org.spring.file.transfer.async.commons.ErrorShowType;
import lombok.Getter;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/27 下午9:52
 */
@Getter
public class TaskException extends RuntimeException {

    private final ErrorShowType errorShowType;

    private final String failReason;

    public TaskException(String failReason) {
        this(ErrorShowType.TOAST, failReason, new RuntimeException(failReason));
    }

    //private final boolean async;
    public TaskException(ErrorShowType errorShowType, String failReason) {
        this(errorShowType, failReason, new RuntimeException(failReason));
    }

    public TaskException(ErrorShowType errorShowType, String failReason, Throwable throwable) {
        super(failReason, throwable);
        this.errorShowType = errorShowType;
        this.failReason = failReason;
        //this.async = async;
    }
}
