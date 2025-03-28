package org.spring.file.transfer.async.core.imports.service;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.AbstractTaskFileService;
import org.spring.file.transfer.async.core.imports.model.ImportFailResult;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;

import java.io.Serializable;

/**
 * @author tiny
 * 
 * @since 2023/5/8 下午3:19
 */
public abstract class AbstractAsyncTaskFileImportService<T extends FileImportModelService, R extends ImportFailResult<T, I>, I extends Serializable, P extends ImportTaskParam> extends AbstractTaskFileService<P>
        implements TaskFileImportService<T, R, I, P> {

    @Override
    public boolean isSupportAsync() {
        return true;
    }

    @Override
    protected TaskType getTaskType() {
        return TaskType.FILE_IMPORT;
    }

}
