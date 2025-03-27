package org.spring.file.transfer.async.core.export.service;

import org.spring.file.transfer.async.core.export.model.ExportPageModel;
import org.spring.file.transfer.async.core.export.model.PrimaryValue;

import java.util.List;

/**
 * @author bm
 */
public abstract class AbstractTaskExcelTemplateExportService<R, P> extends AbstractTaskExcelFilePageExportService<R, P, Long> {


    @Override
    public List<PrimaryValue<Long>> getAllPrimaryValues(P params) {
        return null;
    }

    @Override
    public long totalNum(P params) {
        return 0;
    }

    @Override
    public List<R> doPageExport(ExportPageModel<P> pageModel, PrimaryValue<Long> primaryValue) {
        return null;
    }
}
