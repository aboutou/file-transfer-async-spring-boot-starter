package org.spring.file.transfer.async.core;

import org.spring.file.transfer.async.commons.FileContentType;
import org.spring.file.transfer.async.core.imports.model.FileContentModel;
import org.springframework.http.HttpInputMessage;

/**
 * @author tiny
 * 
 * @since 2023/5/14 上午10:19
 */
public interface FileContentConverter {

    /**
     * 支持的实现
     *
     * @param fileContentType
     * @return
     */
    boolean supports(FileContentType fileContentType);

   /* @Deprecated
    default HttpInputMessage convert(FileContentType fileContentType, String fileContent) {
        FileContentModel fileContent0 = new FileContentModel();
        fileContent0.setFileContent(fileContent);
        fileContent0.setFileContentType(fileContentType);
        return remoteConvert(fileContent0);
    }*/
    /**
     * 远程文件内容转换本地文件
     *
     * @param fileContent
     * @return
     */
    HttpInputMessage remoteConvert(FileContentModel fileContent);

    /**
     * 远程文件内容转换本地文件
     *
     * @param httpInputMessage
     * @return
     */
    default FileContentModel localConvert(HttpInputMessage httpInputMessage) {
        return null;
    }

}
