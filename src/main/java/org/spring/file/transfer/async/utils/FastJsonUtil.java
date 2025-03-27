package org.spring.file.transfer.async.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.List;

/**
 * 兼容 fastjson
 *
 * @author tiny
 */
@Slf4j
public class FastJsonUtil {

    /**
     * 默认jackson对象
     */
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 根据项目需要自定义配置
    static {
        // 解决SerializationFeature.FAIL_ON_EMPTY_BEANS异常
        OBJECT_MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
                // 属性值为null的不参与序列化
                //.setSerializationInclusion(JsonInclude.Include.NON_NULL)
                // 反序列化时忽略对象中不存在的json字段
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        OBJECT_MAPPER.findAndRegisterModules();

        OBJECT_MAPPER.enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        OBJECT_MAPPER.registerModule(new Jdk8Module());
        //OBJECT_MAPPER.registerModule(new JacksonJava8TimeSimpleModule());
        //OBJECT_MAPPER.registerModule(new JacksonOtherSimpleModule());
        /**
         * 忽略反序列化错误
         */
        //OBJECT_MAPPER.addHandler(new NullableFieldsDeserializationProblemHandler());
    }

    /**
     * 将字符串转成对象
     *
     * @param text
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String text, Class<T> clazz) {
        return parseObject(text, clazz, OBJECT_MAPPER);
    }

    public static <T> T parseObject(String text, Class<T> clazz, ObjectMapper objectMapper) {
        T obj = null;
        if (!StringUtils.isEmpty(text)) {
            try {
                obj = objectMapper.readValue(text, clazz);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return obj;
    }

    public static <T> T parseObject(String text, TypeReference<T> valueTypeRef) {
        return parseObject(text, valueTypeRef, OBJECT_MAPPER);
    }

    /**
     * 将字符串转成对象
     *
     * @param text
     * @param valueTypeRef
     * @param objectMapper
     * @param <T>
     * @return
     */
    public static <T> T parseObject(String text, TypeReference<T> valueTypeRef, ObjectMapper objectMapper) {
        T obj = null;
        if (!StringUtils.isEmpty(text)) {
            try {
                obj = objectMapper.readValue(text, valueTypeRef);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return obj;
    }


    /**
     * @Description 字符串转ObjectNode
     **/
    public static ObjectNode toObjectNode(String text) {
        ObjectNode objectNode = null;
        if (!StringUtils.isEmpty(text)) {
            try {
                objectNode = (ObjectNode) OBJECT_MAPPER.readTree(text);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
        return objectNode;
    }

    /**
     * 对象转json
     *
     * @param object
     * @return
     */
    public static String toJsonString(Object object) {
        return toJsonString(object, OBJECT_MAPPER);
    }


    public static String toJsonStringBeautify(Object object) {
        return toJsonString(object, OBJECT_MAPPER.copy().enable(SerializationFeature.INDENT_OUTPUT));
    }

    /**
     * 对象转json
     *
     * @param object
     * @param objectMapper
     * @return
     */
    public static String toJsonString(Object object, ObjectMapper objectMapper) {
        if (object == null) {
            return null;
        }
        if (object instanceof String) {
            return (String) object;
        }
        String res = null;
        try {
            res = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage(), e);
        }
        return res;
    }

    public static <T> List<T> parseArray(String json, Class<T> beanType) {
        if (StringUtils.isEmpty(json)) {
            return null;
        }
        List<T> list;
        try {
            JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructParametricType(List.class, beanType);
            list = OBJECT_MAPPER.readValue(json, javaType);
            return list;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    public static ObjectMapper getObjectMapper() {
        return OBJECT_MAPPER;
    }

}
