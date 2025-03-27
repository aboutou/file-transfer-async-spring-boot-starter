package org.spring.file.transfer.async.core.imports.service;

import org.spring.file.transfer.async.commons.DataResultMode;
import org.spring.file.transfer.async.commons.FileFormat;
import org.spring.file.transfer.async.core.TaskFileService;
import org.spring.file.transfer.async.core.imports.model.ImportFailResult;
import org.spring.file.transfer.async.core.imports.model.ImportResult;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;
import org.springframework.http.HttpInputMessage;

import java.io.Serializable;
import java.util.List;

/**
 * 文件导入抽象
 *
 * @author bm
 */
public interface TaskFileImportService<T extends FileImportModelService, R extends ImportFailResult<T, I>, I extends Serializable, P extends ImportTaskParam> extends TaskFileService<P> {

    /**
     * 文件解析
     *
     * @param param
     * @param inputMessage
     * @return
     */
    List<T> fileParsing(P param, HttpInputMessage inputMessage);

    /**
     * 支持解析的文件格式后缀
     *
     * @return
     */
    List<FileFormat> fileFormats();

    /**
     * 前置处理解析到的数据，第一时间会处理该数据，数据清洗等
     *
     * @param data
     * @param param
     * @return
     */
    List<T> preProcessingData(List<T> data, P param);

    /**
     * 文件校验
     *
     * @param param
     * @param datas
     * @return
     */
    ImportResult<T, R> validate(P param, List<T> datas);

    /**
     * 数据处理的模式，全部成功，部分成功
     *
     * @return
     */
    DataResultMode dataResultMode();

    /**
     * 导入的数据处理
     *
     * @param taskId
     * @param param
     * @param datas
     * @return
     */
    ImportResult<T, R> dataHandle(Serializable taskId, P param, List<T> datas);


}
