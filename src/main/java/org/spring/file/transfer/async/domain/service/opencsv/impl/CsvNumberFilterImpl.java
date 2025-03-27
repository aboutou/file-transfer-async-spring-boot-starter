package org.spring.file.transfer.async.domain.service.opencsv.impl;

import org.spring.file.transfer.async.domain.entities.opencsv.annotation.CsvAnnotation;
import org.spring.file.transfer.async.domain.service.opencsv.CsvFilter;
import org.spring.file.transfer.async.utils.CsvNumberUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;

/**
 * @author bm
 */
public class CsvNumberFilterImpl implements CsvFilter {

    /**
     * 导出数据处理之后，写 文件之前
     *
     * @param value
     * @param field
     * @return
     */
    @Override
    public String handleAfter(String sourceElement, String value, Field field) {
        CsvAnnotation annotation = getCsvAnnotation(field);
        if (annotation == null) {
            return value;
        }
        String prefix = annotation.prefix();
        boolean force = annotation.force();
        if (StringUtils.isNotBlank(prefix)) {
            if (!force) {
                force = CsvNumberUtil.isExcelNumber(StringUtils.trim(sourceElement));
            }
            if (force) {
                value = prefix + value;
            }
        }
        return value;
    }


    private CsvAnnotation getCsvAnnotation(Field field) {
        if (field == null || field.getDeclaredAnnotationsByType(CsvAnnotation.class).length == 0) {
            return null;
        }
        final CsvAnnotation annotation = field.getDeclaredAnnotationsByType(CsvAnnotation.class)[0];
        return annotation;
    }
}
