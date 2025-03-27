package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import org.spring.file.transfer.async.core.export.model.PrimaryValue;

import java.util.List;


/**
 * @author bm
 */
public abstract class AbstractDynamicExcelTemplateExportService<R, P> extends AbstractDynamicExcelAsyncTaskFileExportService<R, P, Long> {


    @Override
    public List<PrimaryValue<Long>> getAllPrimaryValues(P params) {
        return null;
    }

    @Override
    public long totalNum(P params) {
        return 0;
    }

    @Override
    public List<R> doPageDynamicExport(ExportPageModel<P> pageModel, Class<R> clazz, PrimaryValue<Long> primaryValue) {
        return null;
    }

}
