package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.core.AbstractTaskFileService;
import org.spring.file.transfer.async.core.export.model.PrimaryValue;
import org.spring.file.transfer.async.utils.ToStringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpOutputMessage;

import java.io.Serializable;
import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/14 下午10:55
 */
@Slf4j
public abstract class AbstractTaskFileExportService<R, P, I extends Serializable> extends AbstractTaskFileService<P> implements TaskFileExportService<P> {

    @Override
    public void executeExport(Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {
        log.info("id=[{}], 参数=[{}], 开始执行导出", taskId, ToStringUtil.toStringWithAttributes(params, ToStringStyle.JSON_STYLE));
        long totalNum = totalNum(params);
        log.info("id=[{}], 导出总总数为：{}", taskId, totalNum);
        handleExportDataProgress(taskId, totalNum, 0L, 0L);
        // 当导出的数据为空的时候，导出空文件
        if (totalNum <= 0) {
            handleExportData(null, taskId, params, httpOutputMessage);
            return;
        }

        List<PrimaryValue<I>> allPrimaryValues = getAllPrimaryValues(params);
        log.info("id=[{}], 执行按主键或索引导出分片为：{}", taskId, CollectionUtils.size(allPrimaryValues));
        if (allPrimaryValues != null) {
            for (PrimaryValue<I> primaryValue : allPrimaryValues) {
                doExecuteExport(taskId, params, primaryValue, httpOutputMessage);
            }
        } else {
            doExecuteExport(taskId, params, null, httpOutputMessage);
        }
        // boolean page = isPage();
        // boolean supportAppend = isSupportAppend();
    }

    public abstract void doExecuteExport(Serializable taskId, P params, PrimaryValue<I> primaryValue, HttpOutputMessage httpOutputMessage);


    /**
     * 处理数据的情况
     */
    protected void handleExportData(List<R> resultDatas, Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {

    }

    /**
     * 根据条件优先拿到主键相关的值
     *
     * @param params
     * @return
     */
    public abstract List<PrimaryValue<I>> getAllPrimaryValues(P params);

    public abstract long totalNum(P params);


    @Override
    protected TaskType getTaskType() {
        return TaskType.FILE_EXPORT;
    }
}
