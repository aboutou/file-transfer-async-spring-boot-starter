package org.spring.file.transfer.async.commons;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * @author wuzhencheng
 */
@Getter
@Setter
@ToString
@JsonPropertyOrder({"identifier", "errMsg"})
public class FailResult<R extends Serializable> implements Serializable {

    /**
     * 导入数据唯一标识
     * 如果是easypoi，可以查看 IExcelDataModel获取数据所在行号作为唯一标识
     *
     * @return
     * @see cn.afterturn.easypoi.handler.inter.IExcelDataModel
     */
    @ApiModelProperty("数据唯一标识")
    private R identifier;

    @ApiModelProperty("导入失败原因")
    private String errMsg;

    /**
     * 失败的扩展信息
     */
    private LinkedHashMap<String, String> extMap;



    /**
     * @param title 错误的标题
     * @param value 错误的值
     */
    public void addExtErrorValue(String title, Object value) {
        addExtErrorValue(title, String.valueOf(value));
    }

    /**
     * @param title 错误的标题
     * @param value 错误的值
     */
    public void addExtErrorValue(String title, String value) {
        if (this.extMap == null) {
            this.extMap = new LinkedHashMap<>();
        }
        if (StringUtils.isBlank(title)) {
            return;
        }
        this.extMap.put(title, value);
    }
}
