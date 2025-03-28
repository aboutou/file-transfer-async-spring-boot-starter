package org.spring.file.transfer.async.core.imports.service;

import java.io.Serializable;

/**
 * @author tiny
 * 
 * @since 2023/5/8 下午3:21
 */
public interface FileImportModelService {

    /**
     * 导入数据唯一标识
     * 如果是easypoi，可以查看 IExcelDataModel获取数据所在行号作为唯一标识
     *
     *
     * @return
     * @see cn.afterturn.easypoi.handler.inter.IExcelDataModel
     */
    Serializable identifier();

    /**
     * 导入数据唯一描述
     *
     * @return
     */
    String identifierDesc();
}
