package org.spring.file.transfer.async.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author bm
 */
@Getter
@AllArgsConstructor
public enum TaskType {

    FILE_EXPORT("导出"),
    FILE_IMPORT("导入"),

    BATCH_OPERATION("批量操作"),

    ;
    private String desc;
}
