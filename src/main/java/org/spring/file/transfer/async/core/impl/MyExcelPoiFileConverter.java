package org.spring.file.transfer.async.core.impl;

import org.spring.file.transfer.async.commons.FileFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @param <T>
 * @author bm
 */
public class MyExcelPoiFileConverter<T> extends AbstractPoiFileConverter<T> {

    private final static List<FileFormat> fileFormats = Arrays.asList(FileFormat.EXCEL_07, FileFormat.EXCEL_03);

    @Override
    public List<T> doRead(InputStream inputStream, MediaType contentType, Class<? extends T> clzss) {
        return null;
    }

    @Override
    public List<FileFormat> getFileFormats() {
        return fileFormats;
    }

    @Override
    public Workbook getWorkbook(List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat) {
        return null;
    }
}
