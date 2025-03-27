package org.spring.file.transfer.async.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bm
 */
@Getter
@AllArgsConstructor
public enum ErrorShowType {

    CSV,
    EXCEL,
    FILE_URL,
    TOAST,
    CONFIRM,
    NONE,
    ;
}
