package org.spring.file.transfer.async.core.export.handle;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.spring.file.transfer.async.commons.FileContentType;
import org.spring.file.transfer.async.core.FileContentConverter;
import org.spring.file.transfer.async.core.export.service.TaskFileExportService;
import org.spring.file.transfer.async.core.imports.model.FileContentModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author wuzhencheng
 */
@Slf4j
public class AsyncFileExportHandlerImpl extends AbstractFileExportHandler<FileContentModel> {

    private final List<FileContentConverter> fileContentConverters;
    private ObjectMapper objectMapper;

    public AsyncFileExportHandlerImpl(List<TaskFileExportService> taskFileExportServices,
                                      List<FileContentConverter> fileContentConverters) {
        super(taskFileExportServices);
        this.fileContentConverters = fileContentConverters;
    }

    @Override
    public FileContentModel excuteFileHandle(Serializable taskId, FileContentType fileContentType, HttpOutputMessage httpOutputMessage) {
        try {
            OutputStream outputStream = httpOutputMessage.getBody();
            HttpInputMessage httpInputMessage = new HttpInputMessage() {

                @Override
                public HttpHeaders getHeaders() {
                    return httpOutputMessage.getHeaders();
                }

                @Override
                public InputStream getBody() {
                    return getInputStream(outputStream);
                }
            };
            return getFileContentModel(fileContentType, httpInputMessage);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }


    private FileContentModel getFileContentModel(FileContentType fileContentType, HttpInputMessage httpInputMessage) {
        for (FileContentConverter fileContentConverter : fileContentConverters) {
            if (fileContentConverter.supports(fileContentType)) {
                return fileContentConverter.localConvert(httpInputMessage);
            }
        }
        return null;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}
