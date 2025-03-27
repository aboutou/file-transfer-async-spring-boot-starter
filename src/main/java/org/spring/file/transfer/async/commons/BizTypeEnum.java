package org.spring.file.transfer.async.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bm
 */
@Getter
@AllArgsConstructor
public enum BizTypeEnum implements BizType {
    ;

    private final String code;


    @Override
    public String getName() {
        return name();
    }
}
