package org.spring.file.transfer.async.core.impl;

import com.opencsv.CSVWriter;
import com.opencsv.bean.*;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.FileConverter;
import org.spring.file.transfer.async.core.I18nHandler;
import org.spring.file.transfer.async.core.exception.TaskException;
import org.spring.file.transfer.async.core.export.service.TaskFileExportService;
import org.spring.file.transfer.async.domain.service.opencsv.CsvAnnotationCsvWriter;
import org.spring.file.transfer.async.domain.service.opencsv.CsvFilter;
import org.spring.file.transfer.async.domain.service.opencsv.OrderColumnMappingStrategy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/8 上午11:12
 */
@Slf4j
@AllArgsConstructor
public class OpenCsvFileConverter<T> implements FileConverter<T> {


    private final List<CsvFilter> csvFilters;
    private final static FileFormat CSV_FILE_FORMAT = FileFormat.CSV;
    private final I18nHandler i18nHandler;

    @Override
    public boolean canRead(Class<? extends T> clazz, FileFormat fileFormat) {
        return CSV_FILE_FORMAT.equals(fileFormat);
    }

    @Override
    public List<FileFormat> getSupportedFileFormats() {
        return Arrays.asList(CSV_FILE_FORMAT);
    }

    @Override
    public List<T> read(Class<? extends T> clzss, HttpInputMessage inputMessage) {
        InputStream body = null;
        try {
            HeaderColumnNameMappingStrategy<T> mappingStrategy = new HeaderColumnNameMappingStrategy<>();
            mappingStrategy.setType(clzss);
            body = inputMessage.getBody();
            InputStreamReader is = new InputStreamReader(body, StandardCharsets.UTF_8);
            CsvToBean<T> build = new CsvToBeanBuilder<T>(is).withMappingStrategy(mappingStrategy).withSeparator(',').build();
            return build.parse();
        } catch (TaskException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            ExceptionUtils.wrapAndThrow(e);
        } finally {
            IOUtils.closeQuietly(body);
        }
        return null;
    }

    @Override
    public boolean canWrite(Class<? extends T> clazz, FileFormat fileFormat) {
        return CSV_FILE_FORMAT.equals(fileFormat);
    }

    @Override
    public void write(List<T> datas, Class<? extends T> clazz, FileFormat fileFormat, HttpOutputMessage outputMessage) {
        if (datas.size() <= 0) {
            return;
        }
        Writer writer = null;
        CSVWriter csvWriter = null;
        try {
            OpenCsvHttpOutputMessage csvOutputMessage = (OpenCsvHttpOutputMessage) outputMessage;
            Charset charset = csvOutputMessage.getCharset();
            boolean appendFlag = csvOutputMessage.isAppendFlag();
            writer = new OutputStreamWriter(outputMessage.getBody(), charset);
            // utf-8  bom
            /*if (!appendFlag && StandardCharsets.UTF_8.contains(charset)) {
                writer.write(new String(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF}));
            }*/
            // 写内容
            Class<T> cls = (Class<T>) clazz;
            OrderColumnMappingStrategy<T> mappingStrategy = new OrderColumnMappingStrategy<>(cls, appendFlag, i18nHandler);
            List<Field> fields = mappingStrategy.getFields();
            // Map<String, CsvFilter> beans = Optional.ofNullable(SpringContextHolder.getApplicationContext()).map(p -> p.getBeansOfType(CsvFilter.class)).orElse(null);
            List<CsvFilter> values = new ArrayList<>();
            if (csvFilters != null) {
                values.addAll(csvFilters);
                AnnotationAwareOrderComparator.sort(values);
            }
            csvWriter = new CsvAnnotationCsvWriter(writer, CSVWriter.DEFAULT_SEPARATOR, CSVWriter.DEFAULT_QUOTE_CHARACTER,
                    CSVWriter.NO_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END, fields, values);

            // csvWriter.setResultService(new CsvAnnotationResultSetHelperService());
            // 处理标头问题
            if (appendFlag) {
                ((CsvAnnotationCsvWriter) csvWriter).setLength(1);
            }

            StatefulBeanToCsvBuilder<T> builder = new StatefulBeanToCsvBuilder<>(csvWriter);
            Arrays.stream(cls.getDeclaredFields()).filter(one -> {
                one.setAccessible(true);
                return one.isAnnotationPresent(CsvIgnore.class);
            }).forEach(p -> builder.withIgnoreField(cls, p));
            StatefulBeanToCsv<T> beanToCsv = builder.withMappingStrategy(mappingStrategy).build();
            beanToCsv.write(datas);
        } catch (TaskException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            ExceptionUtils.wrapAndThrow(e);
        } finally {
            IOUtils.closeQuietly(csvWriter, writer);
        }
    }

    public static class OpenCsvHttpOutputMessage implements HttpOutputMessage {

        private final OutputStream outputStream;

        private final HttpHeaders header;

        private final Charset charset;

        private final MediaType mediaType;

        private boolean appendFlag;


        public OpenCsvHttpOutputMessage(OutputStream outputStream, Charset charset, boolean appendFlag) {
            this.outputStream = outputStream;
            this.charset = Optional.ofNullable(charset).orElse(StandardCharsets.UTF_8);
            this.header = new HttpHeaders();
            this.mediaType = new MediaType("text", "csv", charset);
            this.header.add(HttpHeaders.CONTENT_TYPE, mediaType.toString());
            this.appendFlag = appendFlag;

            // utf-8  bom
            if (StandardCharsets.UTF_8.contains(charset)) {
                try {
                    outputStream.write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
                } catch (IOException e) {
                    //ignore exception
                }
            }
        }

        @Override
        public OutputStream getBody() {
            return this.outputStream;
        }

        @Override
        public HttpHeaders getHeaders() {
            return this.header;
        }

        public Charset getCharset() {
            return charset;
        }

        public boolean isAppendFlag() {
            return appendFlag;
        }

        public void append() {
            this.appendFlag = true;
        }

        public void disableAppend() {
            this.appendFlag = false;
        }

        public void addFileName(String fileName) {
            this.header.add(TaskFileExportService.FILE_NAME_HEADER, fileName);
        }

    }

    public static void main(String[] args) {
        MediaType mediaType = new MediaType("text", "csv", StandardCharsets.UTF_8);
        System.out.println(mediaType);
        //System.out.println(mediaType.());
    }
}
