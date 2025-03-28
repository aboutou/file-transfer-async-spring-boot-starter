package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.commons.FileContentType;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.TaskFileService;
import org.springframework.http.HttpOutputMessage;

import java.io.OutputStream;
import java.io.Serializable;

/**
 * @author tiny
 * 
 * @since 2023/5/14 下午10:07
 */
public interface TaskFileExportService<P> extends TaskFileService<P> {


    String FILE_NAME_HEADER = "FILE-NAME";
    String FILE_EXTENSION_NAME_HEADER = "FILE-EXTENSION-NAME";

    /**
     * 执行导出
     *
     * @param taskId
     * @param params
     * @param httpOutputMessage
     */
    void executeExport(Serializable taskId, P params, HttpOutputMessage httpOutputMessage);


    default boolean compression() {
        return false;
    }


    HttpOutputMessage getHttpOutputMessage(OutputStream outputStream);

    /**
     * 支持解析的文件格式后缀
     *
     * @return
     */
    FileFormat fileFormat();


    /**
     * 文件名称
     * @return
     */
    String fileName();

    /**
     * 文件最终的类型
     * @return
     */
    default FileContentType fileContentType() {
        return FileContentType.S3_PATH;
    }
}
