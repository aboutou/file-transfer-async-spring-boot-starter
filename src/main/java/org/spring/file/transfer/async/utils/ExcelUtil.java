package org.spring.file.transfer.async.utils;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.entity.enmus.ExcelType;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.result.ExcelImportResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * @author tiny
 */
@Slf4j
public abstract class ExcelUtil {

    public static final ImportParams PARAMS = new ImportParams();

    static {
        PARAMS.setHeadRows(1);
        //ClassExcelVerifyHandler verifyHandler = new ClassExcelVerifyHandler();
        //PARAMS.setVerifyHandler(verifyHandler);
        //PARAMS.setI18nHandler(new I18nExcelCellHeaderHandler());
    }


    public static Workbook getSheets(List<String> dropDownList1, List<String> dropDownList2, List<List<String>> rows, List<String> title) {
        Workbook workbook = new HSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        if (CollectionUtils.isNotEmpty(dropDownList1)) {
            // 在第二列创建下拉框
            String[] items = dropDownList1.toArray(new String[0]);
            DVConstraint constraint = DVConstraint.createExplicitListConstraint(items);
            CellRangeAddressList addressList = new CellRangeAddressList(1, 1000, 1, 1); // 表示单元格B2
            DataValidation dataValidation = new HSSFDataValidation(addressList, constraint);
            dataValidation.setShowErrorBox(true);
            sheet.addValidationData(dataValidation);
        }

        if (CollectionUtils.isNotEmpty(dropDownList2)) {
            // 在第一列创建下拉框
            String[] items2 = dropDownList2.toArray(new String[0]);
            DVConstraint constraint2 = DVConstraint.createExplicitListConstraint(items2);
            CellRangeAddressList addressList2 = new CellRangeAddressList(1, 1000, 0, 0);
            DataValidation dataValidation2 = new HSSFDataValidation(addressList2, constraint2);
            dataValidation2.setShowErrorBox(true);
            sheet.addValidationData(dataValidation2);
        }

        // 创建标题行
        Row titleRow = sheet.createRow(0);
        for (int i = 0; i < title.size(); i++) {
            Cell cell = titleRow.createCell(i);
            cell.setCellValue(title.get(i));
        }

        // 创建数据行
        for (int i = 0; i < rows.size(); i++) {
            Row row = sheet.createRow(i + 1);
            List<String> rowData = rows.get(i);
            for (int j = 0; j < rowData.size(); j++) {
                Cell cell = row.createCell(j);
                cell.setCellValue(rowData.get(j));
            }
        }
        return workbook;
    }

    public static Workbook exportExcel(String title, List<String> headers, List<List<String>> values) {
        List<ExcelExportEntity> colList = new ArrayList<ExcelExportEntity>();
        //I18nExcelCellHeaderHandler defaultI18nHandler = I18nExcelCellHeaderHandler.getDefaultI18nHandler();
        for (int i = 0; i < headers.size(); i++) {
            //  String h = defaultI18nHandler.getLocaleName(headers.get(i));
            String h = headers.get(i);
            ExcelExportEntity colEntity = new ExcelExportEntity(h, i + "");
            colEntity.setNeedMerge(true);
            colList.add(colEntity);
        }
        return exportExcelEntity(title, colList, values);
    }

    public static Workbook exportExcelEntity(String title, List<ExcelExportEntity> colEntities, List<List<String>> values) {
        List<Map<String, Object>> lists = new ArrayList<Map<String, Object>>();
        if (CollectionUtils.isNotEmpty(values)) {
            for (int i = 0; i < values.size(); i++) {
                List<String> vas = values.get(i);
                Map<String, Object> m = new LinkedHashMap<>();
                for (int j = 0; j < vas.size(); j++) {
                    m.put(j + "", vas.get(j));
                }
                lists.add(m);
            }
        } else {
            Map<String, Object> m = new HashMap<>();
            m.put("0", "未查询到数据");
            lists.add(m);
        }
        ExportParams entity = new ExportParams();
        entity.setType(ExcelType.XSSF);
        if (StringUtils.isNotBlank(title)) {
            entity.setSheetName(title);
        }
        Workbook workbook = ExcelExportUtil.exportExcel(entity, colEntities, lists);
        return workbook;
    }

    public static <T> List<T> getExcel(InputStream in, Class<T> clazz) {
        return getExcel(in, clazz, PARAMS);
    }

    public static <T> List<T> getExcel(InputStream in, Class<T> clazz, ImportParams params) {
        try {
            return ExcelImportUtil.importExcel(in, clazz, params);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    public static <T> ExcelImportResult<T> getExcelSheet(InputStream in, Class<T> clazz) {
        try {
            return ExcelImportUtil.importExcelMore(in, clazz, PARAMS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 生成一个多sheet的excel文件
     */
    public static void generateExcel(OutputStream os, List<Map<String, Object>> exportResults) {
        Workbook workbook = generateExcel(exportResults);
        try {
            workbook.write(os);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            if (os != null) {
                IOUtils.closeQuietly(os);
            }
        }
    }

    /**
     * 生成一个多sheet的excel文件
     */
    public static Workbook generateExcel(List<Map<String, Object>> exportResults) {
        return generateExcel(exportResults, ExcelType.XSSF);
    }

    /**
     * 生成一个多sheet的excel文件
     */
    public static Workbook generateExcel(List<Map<String, Object>> exportResults, ExcelType excelType) {
        if (excelType == null) {
            excelType = ExcelType.XSSF;
        }
        Workbook workbook = ExcelExportUtil.exportExcel(exportResults, excelType);
        return workbook;
    }


    public static boolean isExcel2003(String filePath) {
        return filePath.matches("^.+\\.(?i)(xls)$");
    }

    public static boolean isExcel2007(String filePath) {
        return filePath.matches("^.+\\.(?i)(xlsx)$");
    }

}
