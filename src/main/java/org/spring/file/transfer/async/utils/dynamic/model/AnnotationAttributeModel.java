package org.spring.file.transfer.async.utils.dynamic.model;


import lombok.*;

/**
 * @author bm
 */
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AnnotationAttributeModel {

    private String attributeName;
    private Object attributeValue;
}
