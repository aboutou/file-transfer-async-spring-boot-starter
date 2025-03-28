package org.spring.file.transfer.async.domain.entities.opencsv.annotation;

import java.lang.annotation.*;

/**
 * @author tiny
 *  解决数字太大变成科学记数法
 * @since 2021/11/19 上午11:15
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface CsvAnnotation {

    String prefix() default "=";

    /**
     * 强制标识该字段为字符串
     *
     * @return
     */
    boolean force() default false;
}
