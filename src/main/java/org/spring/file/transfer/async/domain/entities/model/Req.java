package org.spring.file.transfer.async.domain.entities.model;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.TaskType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午11:20
 */
@Getter
@Setter
public class Req<T> implements ResolvableTypeProvider {

    @ApiModelProperty(value = "文件导入：fileExport")
    private TaskType taskType;

    @ApiModelProperty(value = "业务类型")
    private BizType bizType;

    private Boolean async = true;

    private T taskParam;

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(this.getClass(), ResolvableType.forInstance(getTaskParam()));
    }
}
