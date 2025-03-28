package org.spring.file.transfer.async.domain.entities.model;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午11:37
 */
@Getter
@Setter
@ToString
public class Res<R> {

    private Serializable taskId;

    /**
     * 业务类型
     */
    private TaskType taskType;

    /**
     * 业务类型
     */
    private BizType bizType;

    /**
     * 任务状态
     */
    private TaskState taskState;

    /**
     * 任务失败的原因，文件解析错误，系统报错等
     */
    private String failReason;

    /**
     * 总条数
     */
    private Long totalNum;

    /**
     * 成功条数
     */
    private Long sucessNum;

    /**
     * 失败条数
     */
    private Long failNum;

    /**
     * 完成百分比
     */
    private BigDecimal completePercent = BigDecimal.ZERO;

    /**
     * 等待结果标记，如果需要等待结果需要此值
     * 1.需要等待结果 0不需要等待结果只要
     */
    private Integer waitResultFlag;

    /**
     * 是否异步
     */
    private Integer asyncFlag;

    /**
     * 失败的数据
     */
    private List<TaskFailResultInstance> failResults;

    /**
     * 错误展示方式
     */
    private ErrorShowType errorShowType;

    /**
     * 最大轮询次数，当且是异步和需要等待结果的时候才生效
     */
    private Integer maxPollingTimes;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 执行结果
     */
    private R result;


}
