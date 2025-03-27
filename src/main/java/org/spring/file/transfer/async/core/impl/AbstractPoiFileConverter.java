package org.spring.file.transfer.async.core.impl;

import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.FileConverter;
import org.spring.file.transfer.async.core.I18nHandler;
import org.spring.file.transfer.async.core.exception.TaskException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author bm
 */
@Slf4j
public abstract class AbstractPoiFileConverter<T> implements FileConverter<T> {

    public static String FILE_PATH = "file-path";

    protected I18nHandler i18nHandler;
    protected Boolean removeHiddenSheet = false;
    protected Boolean checkHiddenSheet = Boolean.TRUE;


    @Override
    public boolean canRead(Class<? extends T> clazz, FileFormat fileFormat) {
        return getFileFormats().stream().anyMatch(p -> p.equals(fileFormat));
    }

    @Override
    public List<FileFormat> getSupportedFileFormats() {
        return getFileFormats();
    }

    @Override
    public final List<T> read(Class<? extends T> clzss, HttpInputMessage inputMessage) {
        InputStream inputStream = null;
        try {
            HttpHeaders headers = inputMessage.getHeaders();
            MediaType contentType = getContentType(headers);
            inputStream = getFileStream(inputMessage, contentType);
            return doRead(inputStream, contentType, clzss);
        } catch (TaskException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Throwable e1 = NestedExceptionUtils.getMostSpecificCause(e);
            throw new TaskException(ErrorShowType.TOAST, "文件解析失败", e1);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * @param headers
     * @return
     * @see org.apache.poi.poifs.filesystem.FileMagic
     */
    protected MediaType getContentType(HttpHeaders headers) {
        MediaType contentType = headers.getContentType();
        List<FileFormat> fileFormats = getFileFormats();
        if (contentType != null) {
            for (FileFormat fileFormat : fileFormats) {
                if (contentType.isCompatibleWith(fileFormat.getMediaType())) {
                    return contentType;
                }
            }
        }
        String filePath = Optional.ofNullable(headers.getFirst(FILE_PATH)).filter(StringUtils::isNotBlank).orElse(null);
        if (StringUtils.isNotBlank(filePath)) {
            String fileName = FilenameUtils.getExtension(filePath);
            for (FileFormat fileFormat : fileFormats) {
                if (StringUtils.endsWithIgnoreCase(fileName, fileFormat.getFileExtensionName())) {
                    return fileFormat.getMediaType();
                }
            }
        }
        return null;
    }

    public abstract List<T> doRead(InputStream inputStream, MediaType contentType, Class<? extends T> clzss);


    protected InputStream getFileStream(HttpInputMessage inputMessage, MediaType contentType) {
        InputStream inputStream;
        Workbook book = null;
        ByteArrayOutputStream outputStream = null;
        try {
            inputStream = inputMessage.getBody();
            if (contentType == null ||
                    (!contentType.isCompatibleWith(FileFormat.EXCEL_07.getMediaType()) && !contentType.isCompatibleWith(FileFormat.EXCEL_03.getMediaType()))) {
                return inputStream;
            }
            boolean check = checkHiddenSheet();
            boolean remove = removeHiddenSheet();
            List<String> lists = null;
            if (check || remove) {
                book = WorkbookFactory.create(inputStream);
                lists = getHiddenSheetName(book);
            }
            if (check) {
                if (CollectionUtils.isNotEmpty(lists)) {
                    throw new TaskException(ErrorShowType.TOAST, "文件存在隐藏的sheet, 名字为：" + lists.stream().collect(Collectors.joining(",")));
                }
            }
            if (remove) {
                if (CollectionUtils.isNotEmpty(lists)) {
                    for (String p : lists) {
                        book.removeSheetAt(book.getSheetIndex(p));
                    }
                }
            }
            if (book != null) {
                outputStream = new ByteArrayOutputStream();
                book.write(outputStream);
                return new ByteArrayInputStream(outputStream.toByteArray());
            }
            return inputStream;
        } catch (Throwable e) {
            ExceptionUtils.wrapAndThrow(e);
        } finally {
            if (book != null) {
                IOUtils.closeQuietly(book);
            }
            if (outputStream != null) {
                try {
                    outputStream.flush();
                } catch (Exception e) {
                }
                IOUtils.closeQuietly(outputStream);
            }
        }
        return null;
    }

    private List<String> getHiddenSheetName(Workbook book) {
        List<String> lists = new ArrayList<>();
        for (Sheet wb : book) {
            int sheetIndex = book.getSheetIndex(wb);
            boolean sheetHidden = book.isSheetHidden(sheetIndex);
            if (sheetHidden) {
                lists.add(wb.getSheetName());
            }
        }
        return lists;
    }

    protected boolean removeHiddenSheet() {
        return Boolean.TRUE.equals(removeHiddenSheet);
    }

    protected boolean checkHiddenSheet() {
        return Boolean.TRUE.equals(checkHiddenSheet);
    }

    @Override
    public boolean canWrite(Class<? extends T> clazz, FileFormat fileFormat) {
        return getFileFormats().stream().anyMatch(p -> p.equals(fileFormat));
    }


    @Override
    public final void write(List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat, HttpOutputMessage outputMessage) {
        HttpHeaders headers = outputMessage.getHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, fileFormat.getMediaType().toString());
        OutputStream outputStream = null;
        try {
            outputStream = outputMessage.getBody();
            Workbook workbook = getWorkbook(dataSet, clazz, fileFormat);
            doHandleWorkbook(workbook, dataSet, clazz, fileFormat);
            workbook.write(outputStream);
            IOUtils.closeQuietly(workbook);
        } catch (Throwable e) {
            log.info(e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(outputStream);
        }
    }

    public abstract List<FileFormat> getFileFormats();

    public abstract Workbook getWorkbook(List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat);

    protected void doHandleWorkbook(Workbook workbook, List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat) {

    }

    public void setI18nHandler(I18nHandler i18nHandler) {
        this.i18nHandler = i18nHandler;
    }

    public void setRemoveHiddenSheet(Boolean removeHiddenSheet) {
        this.removeHiddenSheet = removeHiddenSheet;
    }

    public void setCheckHiddenSheet(Boolean checkHiddenSheet) {
        this.checkHiddenSheet = checkHiddenSheet;
    }
}
