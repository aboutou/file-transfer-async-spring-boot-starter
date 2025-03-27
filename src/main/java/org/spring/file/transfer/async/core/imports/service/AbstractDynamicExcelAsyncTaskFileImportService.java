package org.spring.file.transfer.async.core.imports.service;

import org.spring.file.transfer.async.utils.dynamic.DynamicClassUtil;
import org.spring.file.transfer.async.utils.dynamic.model.DynamicFieldModel;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;

import java.util.List;
import java.util.Optional;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/15 下午8:19
 */
@Slf4j
public abstract class AbstractDynamicExcelAsyncTaskFileImportService<T extends FileImportModelService, P extends ImportTaskParam> extends AbstractExcelAsyncTaskFileImportService<T, P> {


    @Override
    public Class<T> getReaderClass(P params) {
        List<DynamicFieldModel> fieldList = getFieldList(params);
        try {
            String superclassName = Optional.ofNullable(getSuperclass()).map(Class::getName).orElse(null);
            Class<?> clzss = DynamicClassUtil.generatePrototypeClass(fieldList, getDynamicClassName(), superclassName);
            return (Class<T>) clzss;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    protected Class<?> getSuperclass() {
        return null;
    }

    protected String getDynamicClassName() {
        Class<?> aClass = this.getClass();
        String className = aClass.getName() + RandomUtils.nextInt();
        return className;
    }

    /**
     * 获取动态字段的属性
     *
     * @return
     */
    public abstract List<DynamicFieldModel> getFieldList(P params);


}
