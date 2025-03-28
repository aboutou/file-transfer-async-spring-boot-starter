package org.spring.file.transfer.async.core.imports.service;

import com.google.common.collect.Iterators;
import org.spring.file.transfer.async.commons.DataResultMode;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.FileConverter;
import org.spring.file.transfer.async.core.imports.model.ImportFailResult;
import org.spring.file.transfer.async.core.imports.model.ImportResult;
import org.spring.file.transfer.async.utils.ReflectionUtil;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpInputMessage;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 泛型 T 解析文件的java对象
 * 泛型 P 传入的参数对象
 * 泛型 I 数据库唯一主键或者索引字段
 *
 * @author tiny
 * 
 * @since 2023/5/8 下午4:46
 */
public abstract class AbstractExcelAsyncTaskFileImportService<T extends FileImportModelService, P extends ImportTaskParam> extends AbstractAsyncTaskFileImportService<T, ImportFailResult<T, String>, String, P> {


    @Override
    public List<T> fileParsing(P param, HttpInputMessage inputMessage) {
        List<FileFormat> fileFormats = fileFormats();
        Class<?> clazz = getReaderClass(param);
        for (FileConverter fileConverter : getFileConverters()) {
            for (FileFormat fileFormat : fileFormats) {
                if (fileConverter.canRead(clazz, fileFormat)) {
                    return fileConverter.read(clazz, inputMessage);
                }
            }
        }
        return null;
    }

    private List<FileConverter<?>> getFileConverters() {
        List<FileConverter<?>> result = new ArrayList<>();
        FileConverter customFileConverter = getCustomFileConverter();
        if (customFileConverter != null) {
            result.add(customFileConverter);
        }
        if (CollectionUtils.isNotEmpty(fileConverters)) {
            result.addAll(fileConverters);
        }
        return result;
    }

    @Override
    public List<T> preProcessingData(List<T> data, P param) {
        if (CollectionUtils.isEmpty(data)) {
            return data;
        }
        //拿出最后一条，看下所有值是否为null
        int lastIndex = data.size() - 1;
        T t = data.get(lastIndex);
        if (ReflectionUtil.isNullObject(t, getFieldFilter(param))) {
            data.remove(lastIndex);
            //继续处理最后一条为null的数据，直到最后一条不为null的数据
            return preProcessingData(data, param);
        }
        return data;
    }

    protected Predicate<Field> getFieldFilter(P param) {
        return field -> !StringUtils.equalsIgnoreCase(field.getName(), "rowNum");
    }

    public abstract Class<?> getReaderClass(P param);

    protected FileConverter getCustomFileConverter() {
        return null;
    }

    @Override
    public final ImportResult<T, ImportFailResult<T, String>> validate(P param, List<T> datas) {
        ImportResult<T, ImportFailResult<T, String>> importResult = basicValidate(param, datas);
        List<T> successList = importResult.getSuccessList();
        if (!CollectionUtils.isEmpty(successList)) {
            ImportResult<T, ImportFailResult<T, String>> customImportResult = customValidation(param, successList);
            if (customImportResult != null) {
                List<T> successList1 = customImportResult.getSuccessList();
                importResult.setSuccessList(successList1);
                List<ImportFailResult<T, String>> failList1 = customImportResult.getFailList();
                if (!CollectionUtils.isEmpty(failList1)) {
                    List<ImportFailResult<T, String>> failList = importResult.getFailList();
                    failList.addAll(failList1);
                    importResult.setFailList(failList);
                }
            }
        }
        return importResult;
    }

    protected ImportResult<T, ImportFailResult<T, String>> customValidation(P param, List<T> datas) {
        ImportResult<T, ImportFailResult<T, String>> tmp = new ImportResult<>();
        tmp.setSuccessList(datas);
        return tmp;
    }

    private ImportResult<T, ImportFailResult<T, String>> basicValidate(P param, List<T> datas) {
        ImportResult<T, ImportFailResult<T, String>> tmp = new ImportResult<>();
        List<T> successList = new ArrayList<>();
        List<ImportFailResult<T, String>> failList = new ArrayList<>();
        for (T it : datas) {
            String errMsg = jsr303Validate(it);
            if (StringUtils.isBlank(errMsg)) {
                successList.add(it);
            } else {
                ImportFailResult<T, String> result = new ImportFailResult<>();
                result.setData(it);
                result.setIdentifier(String.valueOf(it.identifier()));
                result.setErrMsg(errMsg);
                handleImportFailResult(result, param, it);
                failList.add(result);
            }
        }
        tmp.setSuccessList(successList);
        tmp.setFailList(failList);
        return tmp;
    }

    public void handleImportFailResult(ImportFailResult result, P param, T t) {

    }

    @Override
    public DataResultMode dataResultMode() {
        return DataResultMode.ALL;
    }

    @Override
    public ImportResult<T, ImportFailResult<T, String>> dataHandle(Serializable taskId, P param, List<T> datas) {
        Iterator<T> iterator = Optional.ofNullable(datas).map(List::listIterator).orElseGet(Lists.<T>newArrayList()::listIterator);

        int size = Optional.ofNullable(datas).map(List::size).orElseGet(() -> Iterators.size(iterator));

        Iterator<T> proxyIterator = new Iterator<T>() {

            private int index = 0;
            private static final int NUM = 100;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                int num = index++ % NUM;
                if (num == 0 && size != index) {
                    // 先推送处理数据，到最后才纠正成功数和失败数
                    handleExportDataProgress(taskId, 0, NUM, 0);
                } else if (size == index && num > 0) {
                    handleExportDataProgress(taskId, 0, num, 0);
                }
                return iterator.next();
            }

            @Override
            public void remove() {
                iterator.remove();
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                iterator.forEachRemaining(action);
            }
        };

        return dataHandle(taskId, param, proxyIterator);
    }

    /**
     * 为了进度条的数据
     *
     * @param taskId
     * @param param
     * @param iterator
     * @return
     */
    public ImportResult<T, ImportFailResult<T, String>> dataHandle(Serializable taskId, P param, Iterator<T> iterator) {
        return null;
    }


}
