package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import lombok.AllArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.spring.file.transfer.async.core.impl.OpenCsvFileConverter;
import org.springframework.http.HttpOutputMessage;

import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 泛型 R 返回的结果对象
 * 泛型 P 传入的参数对象
 * 泛型 I 数据库唯一主键或者索引字段
 *
 * @author tiny
 * 
 * @since 2023/5/15 下午1:45
 */
@AllArgsConstructor
public abstract class AbstractTaskCsvFilePageExportService<R, P, I extends Serializable> extends AbstractTaskFilePageExportService<R, P, I> {

    /**
     * 处理数据
     *
     * @param resultDatas
     * @param pageData
     * @param taskId
     * @param pageModel
     * @param httpOutputMessage
     */
    @Override
    public void handlePageExportData(List<R> resultDatas, List<R> pageData, Serializable taskId, ExportPageModel<P> pageModel, HttpOutputMessage httpOutputMessage) {
        if (CollectionUtils.isEmpty(pageData)) {
            return;
        }
        if (httpOutputMessage instanceof OpenCsvFileConverter.OpenCsvHttpOutputMessage) {
            OpenCsvFileConverter.OpenCsvHttpOutputMessage openCsvHttpOutputMessage = (OpenCsvFileConverter.OpenCsvHttpOutputMessage) httpOutputMessage;
            if (pageModel.getPageNo() == 1) {
                openCsvHttpOutputMessage.disableAppend();
            } else {
                openCsvHttpOutputMessage.append();
            }
        }
        handleExportData(pageData, taskId, pageModel.getPageParam(), httpOutputMessage);
    }





    @Override
    public HttpOutputMessage getHttpOutputMessage(OutputStream outputStream) {
        return new OpenCsvFileConverter.OpenCsvHttpOutputMessage(outputStream, StandardCharsets.UTF_8, false);
    }

    @Override
    public FileFormat fileFormat() {
        return FileFormat.CSV;
    }


}
