package org.spring.file.transfer.async.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * @author tiny
 * 
 * @since 2023/5/12 下午4:13
 */
@Getter
//@AllArgsConstructor
public enum TaskState {


    失败(-50, 100),

    新建(10, 0),

    排队中(15, 3),

    执行中(20, 5),

    文件获取中(20110, 3),
    文件解析中(20120, 4),
    数据验证中(20130, 6),
    数据处理中(20140, 10),


    数据获取中(20210, 5),
    文件生成中(20220, 90),


    执行完成(50, 100),

    ;

    @JsonValue
    private Short state;

    private BigDecimal percentage;

    TaskState(int state, int percentage) {
        this((short) state, new BigDecimal(percentage));
    }

    TaskState(short state, BigDecimal percentage) {
        this.state = state;
        this.percentage = percentage;
    }

    public static boolean isComplete(TaskState state) {
        if (TaskState.失败.equals(state) || TaskState.执行完成.equals(state)) {
            return true;
        }
        return false;
    }

    public static boolean isDoing(short type) {
        return isDoing(taskState(type));
    }

    public static boolean isDoing(TaskState state) {
        if (TaskState.文件获取中.equals(state)
                || TaskState.文件获取中.equals(state)
                || TaskState.数据验证中.equals(state)
                || TaskState.数据处理中.equals(state)
                || TaskState.数据获取中.equals(state)
                || TaskState.文件生成中.equals(state)
        ) {
            return true;
        }
        return false;
    }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TaskState taskState(short type) {
        for (TaskState value : values()) {
            if (value.state.shortValue() == type) {
                return value;
            }
        }
        return null;
    }

}
