package org.spring.file.transfer.async.domain.events;

import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.event.EventListener;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午5:04
 */
@Slf4j
@AllArgsConstructor
public class TaskChanagedEventListener {


    @EventListener
    public void handleTaskChanaged(TaskChanagedEvent taskChanagedEvent) {
        Serializable taskId = taskChanagedEvent.getTaskId();
        if (taskId == null) {
            log.error("taskChanagedEvent taskId is null {}", taskChanagedEvent);
            return;
        }

        TaskType taskType = taskChanagedEvent.getTaskType();
        TaskRepository taskRepository = taskChanagedEvent.getTaskRepository();
        TaskInstance taskInstance = taskRepository.find(taskType, taskId);
        if (taskInstance == null) {
            log.info("taskInstance is null {}", taskChanagedEvent);
            return;
        }

        // 判断下该任务单是否已经完成
        if (TaskState.isComplete(taskInstance.getTaskState())) {
            log.info("{} taskInstance is Complete {}, 无需更新", taskInstance.getTaskId(), taskChanagedEvent);
            return;
        }

        // 处理总数
        Long totalEvent = Optional.ofNullable(taskChanagedEvent.getTotalNum()).orElse(0L);
        Long total = Optional.ofNullable(taskInstance.getTotalNum()).orElse(0L);
        taskInstance.setTotalNum(totalEvent + total);

        // 处理成功数
        Long successEvent = Optional.ofNullable(taskChanagedEvent.getSucessNum()).orElse(0L);
        Long success = Optional.ofNullable(taskInstance.getSucessNum()).orElse(0L);
        long sucessNum = successEvent + success;
        if (taskChanagedEvent.isFullSucessNum() && successEvent > 0) {
            sucessNum = successEvent;
        }
        taskInstance.setSucessNum(sucessNum);

        // 处理失败的信息
        Long failedEvent = Optional.ofNullable(taskChanagedEvent.getFailNum()).orElse(0L);
        Long failed = Optional.ofNullable(taskInstance.getFailNum()).orElse(0L);
        long size = CollectionUtils.size(taskChanagedEvent.getFailResults());
        long failNum = failedEvent + failed;
        if (failedEvent == size && size > 0) {
            taskInstance.setFailNum(failNum);
            List<TaskFailResultInstance> failResults = Optional.ofNullable(taskInstance.getFailResults()).orElseGet(ArrayList::new);
            failResults.addAll(taskChanagedEvent.getFailResults());
            taskInstance.setFailResults(failResults);
        }

        Optional<TaskState> taskState = Optional.ofNullable(taskChanagedEvent.getTaskState());
        if (taskState.isPresent()) {
            taskInstance.setTaskState(taskChanagedEvent.getTaskState());
            if (TaskState.失败.equals(taskChanagedEvent.getTaskState())) {
                taskInstance.setFailNum(taskInstance.getTotalNum());
                taskInstance.setSucessNum(0L);
                taskInstance.setFailReason(taskChanagedEvent.getFailReason());
            }
        }


        //算进度 进度=状态机固定进度+条数的进度
        BigDecimal totalBig = Optional.ofNullable(taskInstance.getTotalNum()).map(BigDecimal::new).orElse(BigDecimal.ZERO);
        if (totalBig.compareTo(BigDecimal.ZERO) == 0) {
            taskInstance.setCompletePercent(BigDecimal.ZERO);
        } else {
            BigDecimal bigDecimal = new BigDecimal(sucessNum + failNum);
            BigDecimal completePercent = bigDecimal.divide(totalBig, 2, RoundingMode.HALF_UP);
            if (completePercent.compareTo(BigDecimal.ONE) > 0) {
                completePercent = BigDecimal.ONE;
            }
            taskInstance.setCompletePercent(completePercent);
        }
        BigDecimal completePercent = taskInstance.getCompletePercent();
        BigDecimal statePercent = Optional.ofNullable(taskInstance.getTaskState()).map(TaskState::getPercentage).orElse(BigDecimal.ZERO);

        BigDecimal allPercent = new BigDecimal(100);
        BigDecimal add = statePercent.divide(allPercent).add(completePercent.multiply(allPercent.subtract(statePercent)).divide(allPercent));
        taskInstance.setCompletePercent(add);
        // 完成进度计算

        if (taskChanagedEvent.getErrorShowType() != null) {
            taskInstance.setErrorShowType(taskChanagedEvent.getErrorShowType());
        }
        // 如果已经完成了，或者失败了，就更新为100%
        if (TaskState.isComplete(taskInstance.getTaskState())) {
            taskInstance.setCompletePercent(BigDecimal.ONE);
        }
        // 处理结果
        if (taskChanagedEvent instanceof TaskResultChanagedEvent) {
            TaskResultChanagedEvent resultChanagedEvent = (TaskResultChanagedEvent) taskChanagedEvent;
            if (Objects.nonNull(resultChanagedEvent.getResult())) {
                taskInstance.setResult(resultChanagedEvent.getResult());
            }
        }
        taskInstance.setUpdateTime(LocalDateTime.now());
        taskRepository.update(taskType, taskId, taskInstance);
    }
}
