package org.spring.file.transfer.async.utils.dynamic.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

/**
 * @author bm
 */
@Getter
@Setter
@ToString
public class DynamicFieldModel {

    private String fieldName;
    private Class<?> fieldClass;
    private List<AnnotationFieldModel> annotations;


    public DynamicFieldModel add(AnnotationFieldModel annotationFieldModel) {
        if (annotations == null) {
            annotations = new ArrayList<>();
        }
        annotations.add(annotationFieldModel);
        return this;
    }
}
