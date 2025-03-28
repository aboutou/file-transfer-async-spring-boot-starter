package org.spring.file.transfer.async.core.export.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.FileConverter;
import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import org.spring.file.transfer.async.core.export.model.PrimaryValue;
import org.spring.file.transfer.async.utils.ClassUtil;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpOutputMessage;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * @author tiny
 * 
 * @since 2023/5/15 上午10:15
 */
@Slf4j
public abstract class AbstractTaskFilePageExportService<R, P, I extends Serializable> extends AbstractTaskFileExportService<R, P, I> {


    @Override
    public void doExecuteExport(Serializable taskId, P params, PrimaryValue<I> primaryValue, HttpOutputMessage httpOutputMessage) {
        ExportPageModel<P> page = new ExportPageModel<>(params);
        handleExportPage(page);
        List<R> result = new ArrayList<>();
        List<R> rs = pageExport(result, taskId, page, primaryValue, httpOutputMessage);
        handleAllExportData(rs, taskId, params, httpOutputMessage);
    }

    public List<R> pageExport(List<R> result, Serializable taskId, ExportPageModel<P> pageModel, PrimaryValue<I> primaryValue, HttpOutputMessage httpOutputMessage) {
        log.info("id=[{}], 执行按分片分页为, page:{}, size:{} ,nextPage:{}", taskId, pageModel.getPageNo(), pageModel.getPageSize(), pageModel.isNextPage());
        List<R> t = doPageExport(pageModel, primaryValue);
        int successNum = getPageSuccessNum(pageModel, primaryValue, t);
        handleExportDataProgress(taskId, 0, successNum, 0);
        log.info("id=[{}], 执行按分片分页为, page:{}, size:{}, nextPage:{}, successNum:{}", taskId, pageModel.getPageNo(), pageModel.getPageSize(), pageModel.isNextPage(), successNum);
        if (CollectionUtils.isEmpty(t)) {
            return result;
        }
        handlePageExportData(result, t, taskId, pageModel, httpOutputMessage);
        if (pageModel.hasNextPage()) {
            pageModel.nextPage();
            return pageExport(result, taskId, pageModel, primaryValue, httpOutputMessage);
        }
        return result;
    }

    protected int getPageSuccessNum(ExportPageModel<P> pageModel, PrimaryValue<I> primaryValue, List<R> pageData) {
        if (pageModel.getSuccessPageNum() > 0) {
            return pageModel.getSuccessPageNum();
        }
        int successNum = CollectionUtils.size(pageData);
        return successNum;
    }


    protected void handleExportPage(ExportPageModel<P> page) {

    }


    /**
     * 数据写入
     *
     * @param pageData
     * @param writerClass
     * @param httpOutputMessage
     */
    protected void writeData(List<R> pageData, Class<R> writerClass, HttpOutputMessage httpOutputMessage) {
        FileFormat fileFormat = fileFormat();
        for (FileConverter fileConverter : getFileConverters()) {
            if (fileConverter.canWrite(writerClass, fileFormat)) {
                fileConverter.write(pageData, writerClass, fileFormat, httpOutputMessage);
                return;
            }
        }
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

    protected FileConverter getCustomFileConverter() {
        return null;
    }


    protected void handleAllExportData(List<R> datas, Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {

    }

    @Override
    protected void handleExportData(List<R> datas, Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {
        R r = null;
        if (CollectionUtils.isNotEmpty(datas)) {
            r = datas.get(0);
        }
        Class<R> writerClass = getWriterClass(r, params);
        if (CollectionUtils.isEmpty(datas)) {
            r = ClassUtil.newInstance(writerClass);
            datas = Lists.newArrayList(r);
        }
        writeData(datas, writerClass, httpOutputMessage);
    }

    /**
     * 处理分页数据
     *
     * @param resultDatas
     * @param pageData
     * @param pageModel
     * @param httpOutputMessage
     */
    public abstract void handlePageExportData(List<R> resultDatas, List<R> pageData, Serializable taskId, ExportPageModel<P> pageModel, HttpOutputMessage httpOutputMessage);


    public abstract List<R> doPageExport(ExportPageModel<P> pageModel, PrimaryValue<I> primaryValue);


    protected Class<R> getWriterClass(R r, P params) {
        if (r != null) {
            return (Class<R>) r.getClass();
        }
        Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(this.getClass(), AbstractTaskFilePageExportService.class);
        if (ArrayUtils.isEmpty(typeArgs)) {
            log.error("generice type of {} should be a non-null concrete class", this.getClass().getName());
            Assert.isTrue(false, "导出的泛型需要必填");
        }
        return (Class<R>) typeArgs[0];
    }
}
