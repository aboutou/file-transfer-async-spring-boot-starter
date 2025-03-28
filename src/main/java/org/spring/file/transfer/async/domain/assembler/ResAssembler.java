package org.spring.file.transfer.async.domain.assembler;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.ErrorShowType;
import org.spring.file.transfer.async.commons.TaskState;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.entities.model.Res;
import org.spring.file.transfer.async.utils.CsvNumberUtil;
import org.spring.file.transfer.async.web.dto.output.AsyncTaskResultOutput;
import com.xkzhangsan.time.converter.DateTimeConverterUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author tiny
 * 
 * @since 2023/5/18 下午2:07
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationPackage = "<PACKAGE_NAME>.impl")
public interface ResAssembler {

    @Mapping(target = "executeDuration", expression = "java(this.calculate(res.getCreateTime(),res.getUpdateTime()))")
    //@Mapping(target = "failResults", expression = "java(this.failResults(res))")
    @Mapping(target = "failResultMaps", expression = "java(this.failResultMaps(res))")
    @Mapping(target = "taskTypeDesc", expression = "java(this.convertType(res.getTaskType()))")
    @Mapping(target = "projectName", expression = "java(org.spring.file.transfer.async.utils.SpringContextHolderUtil.getAppName())")
    @Mapping(target = "taskStateDesc", expression = "java(java.util.Optional.ofNullable(res.getTaskState()).map(p -> p.name()).orElse(null))")
    @Mapping(target = "planFinishTime", expression = "java(this.calcPlanFinishTime(res))")
    AsyncTaskResultOutput convert(Res<String> res);


    /**
     * 预计完成时长 = (( 更新时间 - 创建时间 ) / (完成进度 * 100) * (( 1 - 完成进度) * 100))) - (当前时间-更新时间)
     *
     * @param res
     * @return
     */
    default Long calcPlanFinishTime(Res<String> res) {
        if (!TaskState.isDoing(res.getTaskState())) {
            return 0L;
        }
        if (ObjectUtils.isEmpty(res.getCompletePercent())) {
            return 0L;
        }
        long updateDateMilli = DateTimeConverterUtil.toEpochMilli(res.getUpdateTime());
        Long usedTime = updateDateMilli - DateTimeConverterUtil.toEpochMilli(res.getCreateTime());
        BigDecimal finishPercent = res.getCompletePercent().multiply(new BigDecimal("100"));
        BigDecimal waitPercent = new BigDecimal("100").add(finishPercent.negate());
        if (finishPercent.compareTo(BigDecimal.ZERO) == 0 || res.getCompletePercent().compareTo(BigDecimal.ONE) == 0) {
            return 0L;
        }
        BigDecimal plan = new BigDecimal(usedTime).divide(finishPercent, RoundingMode.HALF_UP).multiply(waitPercent);
        long planFinishTimeMilli = plan.longValue() - (System.currentTimeMillis() - updateDateMilli);
        return Math.max(planFinishTimeMilli, 1000L);
    }

    default String convertType(TaskType taskType) {
        for (TaskType value : TaskType.values()) {
            if (StringUtils.equalsIgnoreCase(value.name(), taskType.name())) {
                return value.getDesc();
            }
        }
        return null;
    }

    default Duration calculate(LocalDateTime startTime, LocalDateTime endtime) {
        if (startTime == null || endtime == null) {
            return null;
        }
        return Duration.between(startTime, endtime);
    }

    default List<Map<String, String>> failResultMaps(Res<String> res) {
        ErrorShowType errorShowType = res.getErrorShowType();
        List<TaskFailResultInstance> failResults = res.getFailResults();
        if (!ErrorShowType.CSV.equals(errorShowType) || CollectionUtils.isEmpty(failResults)) {
            return new ArrayList<>();
        }
        /*TaskFailResultInstance optionalMax = failResults.stream().max(Comparator.comparing(TaskFailResultInstance::getExtMap, (o1, o2) -> {
            int i1 = MapUtils.size(o1);
            int i2 = MapUtils.size(o2);
            return i1 - i2;
        })).orElse(null);*/
        TaskFailResultInstance optionalMax = failResults.get(0);
        // 处理header头
        List<Map<String, String>> results = new ArrayList<>();
        List<String> errFields = Optional.ofNullable(res.getBizType()).map(BizType::errFieldOrder).orElse(null);
        List<String> ignoreFields = Optional.ofNullable(res.getBizType()).map(BizType::ignoreField).orElse(null);
        if (CollectionUtils.isNotEmpty(ignoreFields)) {
            if (ignoreFields.contains("数据唯一标识")) {
                ignoreFields.add("identifier");
            }
            if (ignoreFields.contains("失败原因")) {
                ignoreFields.add("errMsg");
            }
        }
        Map<String, String> failResult = getTitleMap(optionalMax, errFields, ignoreFields);
        results.add(0, failResult);
        if (CollectionUtils.isEmpty(failResults)) {
            return results;
        }
        results.addAll(failResults.stream().map(p -> this.getValueMap(p, errFields, ignoreFields)).collect(Collectors.toList()));
        return results;
    }

    default Map<String, String> getTitleMap(TaskFailResultInstance title, List<String> errFieldOrder, List<String> ignoreFields) {
        Map<String, String> extMap = new LinkedCaseInsensitiveMap<>();
        extMap.put("identifier", "数据唯一标识");
        extMap.put("errMsg", "失败原因");
        if (title.getExtMap() != null) {
            extMap.putAll(title.getExtMap());
        }
        if (CollectionUtils.isNotEmpty(ignoreFields)) {
            ignoreFields.forEach(k -> extMap.remove(k));
        }
        Map<String, String> failResult = new LinkedCaseInsensitiveMap<>();
        int i = 1;
        if (CollectionUtils.isNotEmpty(errFieldOrder)) {
            for (String errKey : errFieldOrder) {
                if (extMap.containsKey(errKey)) {
                    String value = CsvNumberUtil.getCsvData(errKey);
                    if (StringUtils.equalsIgnoreCase("identifier", errKey)) {
                        failResult.put("identifier", value);
                    } else if (StringUtils.equalsIgnoreCase("errMsg", errKey)) {
                        failResult.put("errMsg", value);
                    } else {
                        failResult.put("a" + (i++), value);
                    }
                    extMap.remove(errKey);
                }
            }
        }
        Set<String> keys = extMap.keySet();
        for (String errKey : keys) {
            String value = CsvNumberUtil.getCsvData(extMap.get(errKey));
            if (StringUtils.equalsIgnoreCase("identifier", errKey)) {
                failResult.put("identifier", value);
            } else if (StringUtils.equalsIgnoreCase("errMsg", errKey)) {
                failResult.put("errMsg", value);
            } else {
                failResult.put("a" + (i++), value);
            }
        }
        return failResult;
    }

    default Map<String, String> getValueMap(TaskFailResultInstance vlaue, List<String> errFieldOrder, List<String> ignoreFields) {
        Map<String, String> extMap = new LinkedCaseInsensitiveMap<>();
        Serializable identifier = vlaue.getIdentifier();
        String data;
        if (identifier instanceof String) {
            data = (String) identifier;
        } else {
            data = String.valueOf(identifier);
        }
        extMap.put("identifier", data);
        extMap.put("errMsg", vlaue.getErrMsg());
        if (vlaue.getExtMap() != null) {
            extMap.putAll(vlaue.getExtMap());
        }
        if (CollectionUtils.isNotEmpty(ignoreFields)) {
            ignoreFields.forEach(k -> extMap.remove(k));
        }
        Map<String, String> fail0 = new LinkedCaseInsensitiveMap<>();
        int i = 1;
        if (CollectionUtils.isNotEmpty(errFieldOrder)) {
            for (String errKey : errFieldOrder) {
                if (extMap.containsKey(errKey)) {
                    String value = CsvNumberUtil.getCsvData(extMap.remove(errKey));
                    if (StringUtils.equalsIgnoreCase("identifier", errKey)) {
                        fail0.put("identifier", value);
                    } else if (StringUtils.equalsIgnoreCase("errMsg", errKey)) {
                        fail0.put("errMsg", value);
                    } else {
                        fail0.put("a" + (i++), value);
                    }
                }
            }
        }
        for (Map.Entry<String, String> e : extMap.entrySet()) {
            String k = e.getKey();
            String v = CsvNumberUtil.getCsvData(e.getValue());
            if (StringUtils.equalsIgnoreCase("identifier", k)) {
                fail0.put("identifier", v);
            } else if (StringUtils.equalsIgnoreCase("errMsg", k)) {
                fail0.put("errMsg", v);
            } else {
                fail0.put("a" + (i++), v);
            }
        }
        return fail0;
    }


}
