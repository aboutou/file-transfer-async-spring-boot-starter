package org.spring.file.transfer.async.core.imports.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/8 下午4:42
 */
@Getter
@Setter
@ToString
public class ImportResult<T, R extends ImportFailResult> {

    /**
     * 成功结果集
     */
    private List<T> successList;

    /**
     * 失败数据
     */
    private List<R> failList;
}
