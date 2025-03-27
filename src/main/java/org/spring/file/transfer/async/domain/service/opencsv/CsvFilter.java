package org.spring.file.transfer.async.domain.service.opencsv;

import java.lang.reflect.Field;

/**
 * @author bm
 */
public interface CsvFilter {


    /**
     * 导出数据处理之前
     *
     * @param sourceElement 原值
     * @param value         更改以后的值
     * @param field         字段
     * @return
     */
    default String handleBefore(String sourceElement, String value, Field field) {
        return value;
    }


    /**
     * 导出数据处理之后，写 文件之前
     *
     * @param sourceElement 原值
     * @param value         更改以后的值
     * @param field         字段
     * @return
     */
    default String handleAfter(String sourceElement, String value, Field field) {
        return value;
    }
}
