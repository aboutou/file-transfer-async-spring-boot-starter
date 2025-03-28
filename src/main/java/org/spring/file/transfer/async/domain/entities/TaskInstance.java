package org.spring.file.transfer.async.domain.entities;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午4:09
 */
@Getter
@Setter
@ToString
public class TaskInstance<R> {

    private Serializable taskId;

    /**
     * 任务类型
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
     * 是否异步
     */
    private Integer asyncFlag;

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
    private BigDecimal completePercent;


    /**
     * 等待结果标记，如果需要等待结果需要此值
     * 1.需要等待结果 0不需要等待结果只要
     */
    private Integer waitResultFlag;

    /**
     * 失败的数据
     */
    private List<TaskFailResultInstance> failResults;

    /**
     * 最大轮询次数
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
     * 错误展示方式
     */
    private ErrorShowType errorShowType;

    private R result;


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == null || !(obj instanceof TaskInstance) || getTaskId() == null) {
            return super.equals(obj);
        }
        TaskInstance other = (TaskInstance) obj;
        return Objects.equals(taskId, other.taskId) && Objects.equals(taskType, other.taskType);
    }

}
