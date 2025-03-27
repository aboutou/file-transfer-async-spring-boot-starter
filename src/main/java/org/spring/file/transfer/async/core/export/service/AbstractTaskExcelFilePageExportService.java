package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.DefaultHttpOutputMessage;
import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import org.springframework.http.HttpOutputMessage;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

/**
 * @author bm
 */
public abstract class AbstractTaskExcelFilePageExportService<R, P, I extends Serializable> extends AbstractTaskFilePageExportService<R, P, I> {

    @Override
    public HttpOutputMessage getHttpOutputMessage(OutputStream outputStream) {
        return new DefaultHttpOutputMessage(outputStream);
    }

    @Override
    public FileFormat fileFormat() {
        return FileFormat.EXCEL_07;
    }

    @Override
    public void handlePageExportData(List<R> resultDatas, List<R> pageData, Serializable taskId, ExportPageModel<P> pageModel, HttpOutputMessage httpOutputMessage) {
        resultDatas.addAll(pageData);
        pageData.clear();
    }

    @Override
    protected void handleAllExportData(List<R> datas, Serializable taskId, P params, HttpOutputMessage httpOutputMessage) {
        handleExportData(datas, taskId, params, httpOutputMessage);
    }

}
