package org.spring.file.transfer.async.domain.service.opencsv;

import com.opencsv.bean.*;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.spring.file.transfer.async.core.I18nHandler;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * @author bm
 */
public class OrderColumnMappingStrategy<T> extends ColumnPositionMappingStrategy<T> {

    private boolean appendFlag = false;

    private I18nHandler i18nHandler;

    public OrderColumnMappingStrategy(Class<T> cls) {
        this(cls, false);
    }

    public OrderColumnMappingStrategy(Class<T> cls, boolean appendFlag) {
        this(cls, appendFlag, null);
    }

    public OrderColumnMappingStrategy(Class<T> cls, boolean appendFlag, I18nHandler i18nHandler) {
        setType(cls);
        this.appendFlag = appendFlag;
        this.i18nHandler = i18nHandler;
    }

    @Override
    public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
        super.generateHeader(bean);
        FieldMap<String, Integer, ? extends ComplexFieldMapEntry<String, Integer, T>, T> fieldMap = getFieldMap();
        final int numColumns = fieldMap.values().size();
        if (numColumns == -1) {
            return super.generateHeader(bean);
        }
        if (appendFlag) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }

        String[] header = new String[numColumns + 1];

        BeanField<T, ?> beanField;
        for (int i = 0; i <= numColumns; i++) {
            beanField = findField(i);
            String columnHeaderName = extractHeaderName(beanField);
            header[i] = columnHeaderName;
        }
        // headerIndex.initializeHeaderIndex(new String[numColumns]);
        return header;
    }

    private String extractHeaderName(final BeanField<?, ?> beanField) {
        if (beanField == null || beanField.getField() == null
                || beanField.getField().getDeclaredAnnotationsByType(CsvBindByName.class).length == 0) {
            return StringUtils.EMPTY;
        }
        final CsvBindByName bindByNameAnnotation = beanField.getField()
                .getDeclaredAnnotationsByType(CsvBindByName.class)[0];
        //国际化
        if (i18nHandler != null) {
            return i18nHandler.getLocaleName(bindByNameAnnotation.column());
        }
        return bindByNameAnnotation.column();
    }

    public List<Field> getFields() {
        FieldMap<String, Integer, ? extends ComplexFieldMapEntry<String, Integer, T>, T> fieldMap = getFieldMap();
        final int numColumns = fieldMap.values().size();
        if (numColumns == -1) {
            return null;
        }
        List<Field> fields = new ArrayList<>();
        BeanField<T, ?> beanField;
        for (int i = 0; i < numColumns; i++) {
            beanField = findField(i);
            Field field = beanField.getField();
            fields.add(field);
        }
        return fields;
    }


}
