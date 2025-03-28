package org.spring.file.transfer.async.core;

import org.spring.file.transfer.async.commons.FileFormat;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;

import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/6 下午10:02
 */
public interface FileConverter<T> {


    /**
     * 是否支持文件解析
     *
     * @param clazz      需要解析的对象
     * @param fileFormat 需要解析的文件格式
     * @return
     */
    boolean canRead(Class<? extends T> clazz, FileFormat fileFormat);


    /**
     * 支持的文件格式
     *
     * @return
     */
    List<FileFormat> getSupportedFileFormats();

    /**
     * 文件解析
     *
     * @param clzss        需要解析的类
     * @param inputMessage 需要解析的文件流
     * @return
     */
    List<T> read(Class<? extends T> clzss, HttpInputMessage inputMessage);


    /**
     * 是否支持文件写入
     *
     * @param clazz      需要解析的对象
     * @param fileFormat 需要解析的文件格式
     * @return
     */
    boolean canWrite(Class<? extends T> clazz, FileFormat fileFormat);


    /**
     * 文件写入
     *
     * @param datas
     * @param clazz
     * @param fileFormat
     * @param outputMessage
     */
    void write(List<T> datas, Class<? extends T> clazz, FileFormat fileFormat, HttpOutputMessage outputMessage);
}
