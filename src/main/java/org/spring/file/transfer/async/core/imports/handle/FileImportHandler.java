package org.spring.file.transfer.async.core.imports.handle;

import org.spring.file.transfer.async.core.TaskHandler;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;

/**
 * @author tiny
 * 
 * @since 2023/5/8 下午3:28
 */
public interface FileImportHandler extends TaskHandler {


    /**
     * 文件导入执行
     * @param req
     * @return
     */
    TaskInstance excute(Req<ImportTaskParam> req);
}
