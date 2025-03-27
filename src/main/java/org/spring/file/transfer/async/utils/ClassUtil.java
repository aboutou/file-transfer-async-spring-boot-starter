package org.spring.file.transfer.async.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.objenesis.Objenesis;
import org.springframework.objenesis.ObjenesisStd;
import org.springframework.util.ClassUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;

/**
 * @author tiny
 */
@Slf4j
public class ClassUtil {


    /**
     * 通同匹配
     */
    private static final String RESOURCE_PATTERN = "/**/*.class";
    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory();
    private static final Objenesis objenesis = new ObjenesisStd(true);

    public static Set<Class<?>> scanClass(String basePackage) {
        return scanClass(basePackage, (t) -> true);
    }

    public static Set<Class<?>> scanClass(String basePackage, Predicate<MetadataReader> predicate) {
        Set<Class<?>> sets = new HashSet<>();
        try {
            String searchPaths = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + ClassUtils.convertClassNameToResourcePath(basePackage) + RESOURCE_PATTERN;
            Resource[] resources = resourcePatternResolver.getResources(searchPaths);
            for (Resource resource : resources) {
                Class<?> extracted = extracted(resource, predicate);
                if (null != extracted) {
                    sets.add(extracted);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return sets;
    }

    private static Class<?> extracted(Resource resource, Predicate<MetadataReader> predicate) {
        String className = null;
        try {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            if (predicate.test(metadataReader)) {
                className = metadataReader.getClassMetadata().getClassName();
                Class<?> clz = getClassName(className);
                return clz;
            }
        } catch (Throwable e) {
            // log.info(e.getMessage() + "--" + className, e);
        }
        return null;
    }


    public static Class<?> getClassName(String className) {
        try {
            return Class.forName(className, false, ClassUtil.class.getClassLoader());
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
        return null;
    }

    public static <T> T newInstance(String c) {
        Class<T> clazz = (Class<T>) getClassName(c);
        return newInstance(clazz);
    }

    public static <T> T newInstance(Class<T> c) {
        return objenesis.newInstance(c);
    }

}
