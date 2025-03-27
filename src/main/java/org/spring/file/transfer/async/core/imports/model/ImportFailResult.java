package org.spring.file.transfer.async.core.imports.model;

import org.spring.file.transfer.async.commons.FailResult;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/8 下午6:53
 */
@Getter
@Setter
@ToString
public class ImportFailResult<T, R extends Serializable> extends FailResult<R> {

    @ApiModelProperty("数据内容")
    private T data;

}
