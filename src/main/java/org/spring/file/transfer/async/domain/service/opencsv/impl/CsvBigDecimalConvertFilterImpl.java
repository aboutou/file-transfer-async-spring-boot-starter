package org.spring.file.transfer.async.domain.service.opencsv.impl;

import org.apache.commons.lang3.math.NumberUtils;
import org.spring.file.transfer.async.domain.service.opencsv.CsvFilter;

import java.lang.reflect.Field;

/**
 * @author bm
 */
public class CsvBigDecimalConvertFilterImpl implements CsvFilter {

    /**
     * 导出数据处理之前
     *
     * @param value
     * @param field
     * @return
     */
    @Override
    public String handleBefore(String sourceElement, String value, Field field) {
        if (!NumberUtils.isCreatable(value)) {
            return value;
        }
        return value;
        /*JacksonBigDecimal annotation = getAnnotation(field);
        if (annotation == null) {
            return value;
        }
        int scale = annotation.scale();
        RoundingMode mode = annotation.mode();
        boolean removeDecimalZero = annotation.removeDecimalZero();
        BigDecimal bigDecimal = new BigDecimal(value).setScale(scale, mode);
        if (removeDecimalZero) {
            bigDecimal = BigDecimalUtil.bigDecimalRemoveDecimalZero(bigDecimal);
        }
        return bigDecimal.toString();*/
    }

   /* private JacksonBigDecimal getAnnotation(Field field) {
        if (field == null || field.getDeclaredAnnotationsByType(JacksonBigDecimal.class).length == 0) {
            return null;
        }
        final JacksonBigDecimal annotation = field.getDeclaredAnnotationsByType(JacksonBigDecimal.class)[0];
        return annotation;
    }*/
}
