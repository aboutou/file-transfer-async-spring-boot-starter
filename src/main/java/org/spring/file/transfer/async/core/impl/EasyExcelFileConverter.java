package org.spring.file.transfer.async.core.impl;

import com.alibaba.excel.EasyExcel;
import org.spring.file.transfer.async.commons.FileFormat;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.http.MediaType;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * <dependency>
 * <groupId>com.alibaba</groupId>
 * <artifactId>easyexcel</artifactId>
 * <version>3.3.2</version>
 * </dependency>
 *
 * @author bm
 */
public class EasyExcelFileConverter<T> extends AbstractPoiFileConverter<T> {

    private final static List<FileFormat> fileFormats = Arrays.asList(FileFormat.EXCEL_07, FileFormat.EXCEL_03);

    @Override
    public List<T> doRead(InputStream inputStream, MediaType contentType, Class<? extends T> clzss) {
        List<T> dataList = EasyExcel.read(inputStream).autoCloseStream(true).autoTrim(true).head(clzss).sheet().doReadSync();
        return dataList;
    }

    @Override
    public List<FileFormat> getFileFormats() {
        return fileFormats;
    }

    @Override
    public Workbook getWorkbook(List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat) {
        /*ExcelTypeEnum excelType = ExcelTypeEnum.XLSX;
        if (FileFormat.EXCEL_03.equals(excelType)) {
            excelType = ExcelTypeEnum.XLS;
        } else if (FileFormat.CSV.equals(fileFormat)) {
            excelType = ExcelTypeEnum.CSV;
        }

        EasyExcel.write().head(clazz).excelType(excelType).build().write();*/
        return null;
    }
}
