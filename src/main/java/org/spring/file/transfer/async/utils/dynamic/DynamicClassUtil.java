package org.spring.file.transfer.async.utils.dynamic;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.spring.file.transfer.async.utils.dynamic.model.AnnotationAttributeModel;
import org.spring.file.transfer.async.utils.dynamic.model.AnnotationFieldModel;
import org.spring.file.transfer.async.utils.dynamic.model.DynamicFieldModel;
import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @author tiny
 * 
 * @since 2023/5/14 上午10:55
 */
public abstract class DynamicClassUtil {

    // public static final String ANNOTATION_PACKAGE_NAME = "cn.afterturn.easypoi.excel.annotation.Excel";
    // public static final String STRING_PACKAGE_NAME = "java.lang.String";

    public static Class<?> generatePrototypeClass(List<DynamicFieldModel> list, String className)
            throws Exception {
        return generatePrototypeClass(list, className, null);
    }

    public static Class<?> generatePrototypeClass(List<DynamicFieldModel> list, String className, String superclassName)
            throws Exception {
        //String className = CLASS_NAME_PREFIX + UUID.randomUUID().toString();
        ClassPool pool = ClassPool.getDefault();
        CtClass clazz = pool.makeClass(className);
        if (StringUtils.isNotBlank(superclassName)) {
            CtClass supercClazz = pool.get(superclassName);
            clazz.setSuperclass(supercClazz);
        }
        // 添加fields
        addExpressField(pool, clazz, list);
        //clazz.writeFile("./target/classes");
        //Loader classLoader = new Loader(pool);
        //Class<?> newClass = classLoader.loadClass(clazz.getName());
        //return newClass;
        return clazz.toClass();
    }


    private static void addExpressField(ClassPool pool, CtClass clazz, List<DynamicFieldModel> list) throws CannotCompileException, NotFoundException {
        for (DynamicFieldModel dynamicColumn : list) {
            addFieldAndAnnotation(pool, clazz, dynamicColumn, null);
        }
    }

    private static void addFieldAndAnnotation(ClassPool pool,
                                              CtClass clazz,
                                              DynamicFieldModel dynamicColumn,
                                              Consumer<AnnotationsAttribute> consumer) throws NotFoundException, CannotCompileException {
        String fieldName = dynamicColumn.getFieldName();
        List<AnnotationFieldModel> attributes = dynamicColumn.getAnnotations();
        Class<?> fieldClass = dynamicColumn.getFieldClass();
        // 生成field
        CtField field = new CtField(pool.get(fieldClass.getName()), fieldName, clazz);
        field.setModifiers(Modifier.PRIVATE);
        // 添加easypoi的注解
        ConstPool constpool = field.getFieldInfo().getConstPool();
        AnnotationsAttribute fieldAttr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        if (CollectionUtils.isNotEmpty(attributes)) {
            for (AnnotationFieldModel attribute : attributes) {
                Annotation annotation = new Annotation(attribute.getAnnotationClass(), constpool);
                for (AnnotationAttributeModel attributeAttribute : attribute.getAttributes()) {
                    Object attributeValue = attributeAttribute.getAttributeValue();
                    if (attributeValue instanceof String) {
                        annotation.addMemberValue(attributeAttribute.getAttributeName(), new StringMemberValue(attributeValue.toString(), constpool));
                    } else if (attributeValue instanceof Integer) {
                        annotation.addMemberValue(attributeAttribute.getAttributeName(), new IntegerMemberValue(constpool, (Integer) attributeValue));
                    } else if (attributeValue instanceof Long) {
                        annotation.addMemberValue(attributeAttribute.getAttributeName(), new LongMemberValue((Long) attributeValue, constpool));
                    } else if (attributeValue instanceof String[]) {
                        String[] colValue = (String[]) attributeValue;
                        StringMemberValue[] elements = new StringMemberValue[colValue.length];
                        for (int j = 0; j < colValue.length; j++) {
                            elements[j] = new StringMemberValue(colValue[j], constpool);
                        }
                        ArrayMemberValue arrayMemberValue = new ArrayMemberValue(constpool);
                        arrayMemberValue.setValue(elements);
                        annotation.addMemberValue(attributeAttribute.getAttributeName(), arrayMemberValue);
                    }
                    fieldAttr.addAnnotation(annotation);
                }
            }
        }
        field.getFieldInfo().addAttribute(fieldAttr);
        if (consumer != null) {
            consumer.accept(fieldAttr);
        }
        // 生成get,set方法
        clazz.addMethod(CtNewMethod.getter("get" + upperFirstLatter(fieldName), field));
        clazz.addMethod(CtNewMethod.setter("set" + upperFirstLatter(fieldName), field));

        clazz.addField(field);
    }

    private static String upperFirstLatter(String letter) {
        return letter.substring(0, 1).toUpperCase() + letter.substring(1);
    }

    private static String getFieldValue(String fieldName, Object data) throws Exception {
        Method m = data.getClass().getMethod("get" + upperFirstLatter(fieldName));
        return (String) m.invoke(data);
    }

    public static List<Map<String, String>> parseObjectList(List<?> result) throws Exception {
        List<Map<String, String>> parseResult = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(result)) {
            Class<?> clazz = result.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();
            for (Object data : result) {
                Map<String, String> parseDataMap = Maps.newConcurrentMap();
                for (Field field : fields) {
                    String value = getFieldValue(field.getName(), data);
                    parseDataMap.put(field.getName(), value == null ? "" : value);
                }
                parseResult.add(parseDataMap);
            }
        }
        return parseResult;
    }
}
