package org.spring.file.transfer.async.utils;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * 反射判断对象是否为null
 *
 * @author bm
 */
public abstract class ReflectionUtil {

    public static void setFieldValue(Object obj, String name, Object value) {
        if (obj == null || value == null) {
            return;
        }
        Field field = ReflectionUtils.findField(obj.getClass(), name);
        if (field == null) {
            return;
        }
        ReflectionUtils.makeAccessible(field);
        ReflectionUtils.setField(field, obj, value);
    }

    public static boolean isNullObject(Object obj, Predicate<Field> ff) {
        Class<?> objClass = obj.getClass();
        // Java基本数据类型
        boolean simpleProperty = BeanUtils.isSimpleProperty(objClass);
        if (simpleProperty) {
            return true;
        }
        List<Field> fields = new ArrayList<>();
        ReflectionUtils.doWithFields(objClass, field -> fields.add(field), ReflectionUtils.COPYABLE_FIELDS);
        for (Field f : fields) {
            if (ff != null && !ff.test(f)) {
                continue;
            }
            ReflectionUtils.makeAccessible(f);
            Object fieldValue = ReflectionUtils.getField(f, obj);
            if (fieldValue instanceof String) {
                if (StringUtils.isNotBlank(((String) fieldValue))) {
                    return false;
                }
            }
            if (ObjectUtils.isNotEmpty(fieldValue)) {
                return false;
            }
        }
        return true;
    }
}
