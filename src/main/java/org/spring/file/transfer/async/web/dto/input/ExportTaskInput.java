package org.spring.file.transfer.async.web.dto.input;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午11:29
 */
@Getter
@Setter
public class ExportTaskInput  {

    @ApiModelProperty(value = "业务类型", required = true)
    @NotBlank(message = "业务类型不能为空")
    private String bizType;

    private Boolean async = true;

    @ApiModelProperty(value = "业务查询参数，是一个json字符串")
    private String taskParam;
}
