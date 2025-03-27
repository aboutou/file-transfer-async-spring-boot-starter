package org.spring.file.transfer.async.domain.service.opencsv.impl;


import org.apache.commons.lang3.math.NumberUtils;
import org.spring.file.transfer.async.domain.service.opencsv.CsvFilter;

import java.lang.reflect.Field;

/**
 * @author bm
 */
public class CsvUnitConvertFilterImpl implements CsvFilter {

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
       /* JacksonUnitConvert unitConvert = getJacksonUnitConvert(field);
        if (unitConvert == null) {
            return value;
        }
        String sourceUnit = unitConvert.sourceUnitSerializer();
        String targetUnit = unitConvert.targetUnitSerializer();
        boolean removeDecimalZero = unitConvert.removeDecimalZero();
        if ("".equals(targetUnit)) {
            Class<? extends UnitValueConverter> aclass = unitConvert.targetUnitSerializerClass();
            targetUnit = UnitConverterUtil.getUnitValueConverter(aclass).getSerializerTargetUnit(sourceUnit);
        }
        UnitConverter sourceUnitConverter = UnitHelper.getUnitConverter(StringUtils.upperCase(sourceUnit));
        UnitConverter targetUnitConverter = UnitHelper.getUnitConverter(StringUtils.upperCase(targetUnit));
        double convert = sourceUnitConverter.convert(new BigDecimal(value).doubleValue(), targetUnitConverter);
        BigDecimal bigDecimal = new BigDecimal(String.valueOf(convert)).setScale(unitConvert.scale(), RoundingMode.HALF_UP);
        if (removeDecimalZero) {
            bigDecimal = BigDecimalUtil.bigDecimalRemoveDecimalZero(bigDecimal);
        }
        return bigDecimal.toString();*/
    }

    /*private JacksonUnitConvert getJacksonUnitConvert(Field field) {
        if (field == null || field.getDeclaredAnnotationsByType(JacksonUnitConvert.class).length == 0) {
            return null;
        }
        final JacksonUnitConvert csvAnnotation = field.getDeclaredAnnotationsByType(JacksonUnitConvert.class)[0];
        return csvAnnotation;
    }*/
}
