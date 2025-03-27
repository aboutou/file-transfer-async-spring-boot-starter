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
public class AnnotationFieldModel {


    private String annotationClass;
    private List<AnnotationAttributeModel> attributes;


    public AnnotationFieldModel add(String attributeName, Object attributeValue) {
        return add(new AnnotationAttributeModel(attributeName, attributeValue));
    }

    public AnnotationFieldModel add(AnnotationAttributeModel model) {
        if (attributes == null) {
            attributes = new ArrayList<>();
        }
        attributes.add(model);
        return this;
    }
}
