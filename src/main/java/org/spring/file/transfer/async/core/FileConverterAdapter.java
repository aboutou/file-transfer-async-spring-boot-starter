package org.spring.file.transfer.async.core;


import org.spring.file.transfer.async.commons.FileFormat;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import java.util.List;

/**
 * @author wuzhencheng
 */
public abstract class FileConverterAdapter<T> implements FileConverter<T> {

    @Override
    public boolean canRead(Class<? extends T> clazz, FileFormat fileFormat) {
        return false;
    }

    @Override
    public List<FileFormat> getSupportedFileFormats() {
        return null;
    }

    @Override
    public List<T> read(Class<? extends T> clzss, HttpInputMessage inputMessage) {
        return null;
    }

    @Override
    public boolean canWrite(Class<? extends T> clazz, FileFormat fileFormat) {
        return false;
    }

    @Override
    public void write(List<T> datas, Class<? extends T> clazz, FileFormat fileFormat, HttpOutputMessage outputMessage) {

    }
}
