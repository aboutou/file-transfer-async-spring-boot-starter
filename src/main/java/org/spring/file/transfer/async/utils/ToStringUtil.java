package org.spring.file.transfer.async.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

/**
 * @author wuzhencheng
 */
public abstract class ToStringUtil {


    public static String toStringWithAttributes(Object ofInterest, ToStringStyle style) {
        ReflectionToStringBuilder builder = new ReflectionToStringBuilder(ofInterest, style) {

            @Override
            public boolean accept(final Field field) {
                //ReflectionUtils.makeAccessible(field);
                Object value = ReflectionUtils.getField(field, this.getObject());
                return super.accept(field) && value != null && StringUtils.isNotBlank(String.valueOf(value));
            }

            @Override
            public Object getValue(final Field field) {
                //ReflectionUtils.makeAccessible(field);
                return ReflectionUtils.getField(field, this.getObject());
            }
        };
        return builder.toString();
    }
}
