package org.spring.file.transfer.async.domain.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.spring.file.transfer.async.commons.FailResult;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午4:23
 */
@Getter
@Setter
@ToString
@JsonPropertyOrder({"identifier", "errMsg"})
public class TaskFailResultInstance {

    public TaskFailResultInstance() {
    }

    public TaskFailResultInstance(FailResult result) {
        Objects.requireNonNull(result);
        this.setIdentifier(result.getIdentifier());
        this.setErrMsg(result.getErrMsg());
        this.setExtMap(result.getExtMap());
    }


    /**
     * 失败唯一标识
     */
    @JsonProperty(index = 0)
    private Serializable identifier;

    /**
     * 失败原因
     */
    @JsonProperty(index = 1)
    private String errMsg;

    /**
     * 失败的扩展信息
     */
    private LinkedHashMap<String, String> extMap;


}
