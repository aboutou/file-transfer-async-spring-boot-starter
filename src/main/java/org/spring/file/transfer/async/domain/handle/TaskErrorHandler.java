package org.spring.file.transfer.async.domain.handle;

import lombok.extern.slf4j.Slf4j;
import org.spring.file.transfer.async.commons.BizType;
import org.springframework.core.NestedExceptionUtils;

import java.io.Serializable;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午11:06
 */
@FunctionalInterface
public interface TaskErrorHandler {

    /**
     * Handle the given error, possibly rethrowing it as a fatal exception.
     *
     * @param t
     * @param taskId
     * @param bizType
     * @return Throwable
     */
    Throwable handleError(Throwable t, Serializable taskId, BizType bizType);


    @Slf4j
    class DefaultTaskErrorHandler implements TaskErrorHandler {

        @Override
        public Throwable handleError(Throwable t, Serializable taskId, BizType bizType) {
            log.error(t.getMessage(), t);
            Throwable t1 = NestedExceptionUtils.getMostSpecificCause(t);
          /*  if (t1 instanceof ApiFeignException) {
                ApiFeignException t2 = (ApiFeignException) t1;
                return new TaskException(ErrorShowType.TOAST, t2.getErrorCode().getMessage(), t1);
            }*/
            return t1;
        }

    }
}
