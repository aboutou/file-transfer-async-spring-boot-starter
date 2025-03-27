package org.spring.file.transfer.async.core.export.service;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomUtils;
import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import org.spring.file.transfer.async.core.export.model.PrimaryValue;
import org.spring.file.transfer.async.utils.ClassUtil;
import org.spring.file.transfer.async.utils.dynamic.DynamicClassUtil;
import org.spring.file.transfer.async.utils.dynamic.model.DynamicFieldModel;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpOutputMessage;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author bm
 */
@Slf4j
public abstract class AbstractDynamicExcelAsyncTaskFileExportService<R, P, I extends Serializable> extends AbstractTaskExcelFilePageExportService<R, P, I> {

    private final ThreadLocal<Class<R>> CLASS_THREAD_LOCAL = new ThreadLocal<>();

    @Override
    public final void executeExport(Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {
        try {
            super.executeExport(taskId, params, httpOutputMessage);
        } finally {
            CLASS_THREAD_LOCAL.remove();
        }
    }

    protected Class<R> getWriterDynamicClass(R r, P params) {
        Class<R> classCache = CLASS_THREAD_LOCAL.get();
        if (classCache != null) {
            return classCache;
        }
        List<DynamicFieldModel> fieldList = getFieldList(r, params);
        try {
            String superclassName = Optional.ofNullable(getSuperclass()).map(Class::getName).orElse(null);
            Class<?> clzss = DynamicClassUtil.generatePrototypeClass(fieldList, getDynamicClassName(), superclassName);
            CLASS_THREAD_LOCAL.set((Class<R>) clzss);
            return (Class<R>) clzss;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    protected String getDynamicClassName() {
        Class<?> aClass = this.getClass();
        String className = aClass.getName() + RandomUtils.nextInt();
        return className;
    }

    /**
     * 动态类的父超类
     *
     * @return
     */
    protected Class<?> getSuperclass() {
        Class<?>[] typeArgs = GenericTypeResolver.resolveTypeArguments(this.getClass(), AbstractTaskFilePageExportService.class);
        if (ArrayUtils.isEmpty(typeArgs)) {
            log.error("generice type of {} should be a non-null concrete class", this.getClass().getName());
            Assert.isTrue(false, "导出的泛型需要必填");
        }
        return typeArgs[0];
    }

    @Override
    public final List<R> doPageExport(ExportPageModel<P> pageModel, PrimaryValue<I> primaryValue) {
        Class<R> writerDynamicClass = getWriterDynamicClass(null, pageModel.getPageParam());
        return doPageDynamicExport(pageModel, writerDynamicClass, primaryValue);
    }

    public abstract List<R> doPageDynamicExport(ExportPageModel<P> pageModel, Class<R> clazz, PrimaryValue<I> primaryValue);

    /**
     * 获取动态字段的属性
     *
     * @return
     */
    public abstract List<DynamicFieldModel> getFieldList(R r, P params);

    @Override
    protected void handleExportData(List<R> resultDatas, Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {
        if (CollectionUtils.isNotEmpty(resultDatas)) {
            super.handleExportData(resultDatas, taskId, params, httpOutputMessage);
            return;
        }
        List<R> data = new ArrayList<>();
        Class<R> writerDynamicClass = getWriterDynamicClass(null, params);
        R r = ClassUtil.newInstance(writerDynamicClass);
        data.add(r);
        super.handleExportData(data, taskId, params, httpOutputMessage);
    }
}
