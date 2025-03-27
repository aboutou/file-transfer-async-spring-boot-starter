package org.spring.file.transfer.async.core.impl;

import org.spring.file.transfer.async.commons.FileFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带有下拉选择的excel
 *
 * @author bm
 */
@Slf4j
public abstract class AbstractEasyPoiSelectLargeFileConverter<T> extends EasyPoiFileConverter<T> {


    @Override
    protected void doHandleWorkbook(Workbook workbook, List<T> dataSet, Class<? extends T> clazz, FileFormat fileFormat) {
        List<CellValidation> cellValidations = getCellValidation();
        int sheetIndex = 1;
        for (CellValidation cellValidation : cellValidations) {
            if (cellValidation.getType() == 10) {
                if (CollectionUtils.isEmpty(cellValidation.getSelectList())) {
                    continue;
                }
                selectLargeList(workbook, sheetIndex++, cellValidation.firstRow, cellValidation.lastRow, cellValidation.firstCol, cellValidation.lastCol, cellValidation.getSelectList().toArray(new String[]{}));
            } else if (cellValidation.getType() == 20) {
                if (ObjectUtils.isEmpty(cellValidation.getStringListMap())) {
                    continue;
                }
                selectLargeMapV2(workbook, sheetIndex++, cellValidation.firstRow, cellValidation.lastRow, cellValidation.firstCol, cellValidation.lastCol, cellValidation.getStringListMap());
            }
        }
    }

    /**
     * 用于下拉内容很多，字符超过255
     * 下拉选项（先将数据放到另一个sheet页，然后下拉的数据再去sheet页读取，解决普通下拉数据量太多下拉不显示问题）
     * sheetIndex 创建的sheet的idex。如果有多个下拉想放到sheet页，则需要设置不同的sheetIndex，（注意不能设置为0，0为主数据页）
     * firstRow 起始行(下标0开始)
     * lastRow  终止行，最大65535
     * firstCol 起始列 (下标0开始)
     * lastCol 终止列
     * dataArray 下拉内容
     */
    public static void selectLargeList(Workbook workbook, int sheetIndex, int firstRow, int lastRow, int firstCol, int lastCol, String[] selectList) {
        Sheet sheet = workbook.getSheetAt(0);
        //将下拉框数据放到新的sheet里，然后excle通过新的sheet数据加载下拉框数据
        String hidddenSheetName = "sheetName" + sheetIndex;
        Sheet hidden = workbook.createSheet(hidddenSheetName);

        // 创建单元格对象
        Cell cell = null;
        // 遍历我们上面的数组，将数据取出来放到新sheet的单元格中
        for (int i = 0, length = selectList.length; i < length; i++) {
            // 取出数组中的每个元素
            String name = selectList[i];
            // 根据i创建相应的行对象（说明我们将会把每个元素单独放一行）
            Row row = hidden.createRow(i);
            // 创建每一行中的第一个单元格
            cell = row.createCell(0);
            // 然后将数组中的元素赋值给这个单元格
            cell.setCellValue(name);
        }

        String refers = hidddenSheetName + "!$A$1:$A$" + selectList.length;
        // 设置生效的起始行、终止行、起始列、终止列
        // CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(1, 90000, 1, 1);
        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);

        DataValidation dataValidation = null;
        if (sheet instanceof XSSFSheet || sheet instanceof SXSSFSheet) {
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvHelper.createFormulaListConstraint(refers);
            dataValidation = dvHelper.createValidation(constraint, cellRangeAddressList);
        } else {
            DataValidationConstraint constraint = DVConstraint.createFormulaListConstraint(refers);
            dataValidation = new HSSFDataValidation(cellRangeAddressList, constraint);
        }

        // DataValidationHelper helper = sheet.getDataValidationHelper();
        // DataValidation dataValidation = helper.createValidation(constraint, cellRangeAddressList);

        // 适配xls和xlsx
        if (dataValidation instanceof HSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(false);
        } else {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        }
        // 将第sheetIndex个sheet设置为隐藏
        workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);
        sheet.addValidationData(dataValidation);
    }

    public static void selectLargeMapV2(Workbook workbook, int sheetIndex, int firstRow, int lastRow, int firstCol, int lastCol, Map<String, List<String>> selectListMap) {
        Sheet sheet = workbook.getSheetAt(0);

        String[] keyArr = selectListMap.keySet().toArray(new String[0]);

        Map<String, String[]> paChiMap = new HashMap<String, String[]>();
        selectListMap.forEach((k, v) -> {
            paChiMap.put(k, v.toArray(new String[0]));
        });

        //将下拉框数据放到新的sheet里，然后excle通过新的sheet数据加载下拉框数据
        String hidddenSheetName = "sheetName" + sheetIndex;
        Sheet hideSheet = workbook.createSheet(hidddenSheetName);
        //这一行作用是将此sheet隐藏，功能未完成时注释此行,可以查看隐藏sheet中信息是否正确
        workbook.setSheetHidden(workbook.getSheetIndex(hideSheet), true);

        int rowId = 0;
        // 设置第一行，存省的信息
        Row provinceRow = hideSheet.createRow(rowId++);
        provinceRow.createCell(0).setCellValue("渠道");
        for (int i = 0; i < keyArr.length; i++) {
            Cell provinceCell = provinceRow.createCell(i + 1);
            provinceCell.setCellValue(keyArr[i]);
        }
        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (int i = 0; i < keyArr.length; i++) {
            String key = keyArr[i];
            String[] son = paChiMap.get(key);
            Row row1 = hideSheet.createRow(rowId++);
            row1.createCell(0).setCellValue(key);
            for (int j = 0; j < son.length; j++) {
                Cell cell0 = row1.createCell(j + 1);
                cell0.setCellValue(son[j]);
            }

            // 添加名称管理器
            String range = getRange(1, rowId, son.length);
            Name name = workbook.createName();
            //key不可重复
            name.setNameName(key);
            String formula = hidddenSheetName + "!" + range;
            name.setRefersToFormula(formula);
        }

        CellRangeAddressList cellRangeAddressList = new CellRangeAddressList(firstRow, lastRow, firstCol, firstCol);

        String refers = hidddenSheetName + "!$A$2:$A$" + (keyArr.length + 1);
        DataValidation dataValidation = null;
        if (sheet instanceof XSSFSheet || sheet instanceof SXSSFSheet) {
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvHelper.createFormulaListConstraint(refers);
            dataValidation = dvHelper.createValidation(constraint, cellRangeAddressList);
        } else {
            DataValidationConstraint constraint = DVConstraint.createFormulaListConstraint(refers);
            dataValidation = new HSSFDataValidation(cellRangeAddressList, constraint);
        }

        // 适配xls和xlsx
        if (dataValidation instanceof HSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(false);
        } else {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        }
        dataValidation.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        // 将第sheetIndex个sheet设置为隐藏
//            workbook.setSheetHidden(workbook.getSheetIndex(hidden), true);
        sheet.addValidationData(dataValidation);

        //对前20行设置有效性
        for (int i = 1; i < lastRow; i++) {
            String off = String.valueOf((char) ((char) 'A' + firstCol));
            setDataValidation(off, sheet, i, lastCol);
        }
    }

    /**
     * 设置有效性
     *
     * @param offset 主影响单元格所在列，即此单元格由哪个单元格影响联动
     * @param sheet
     * @param rowNum 行数
     * @param colNum 列数
     */
    private static void setDataValidation(String offset, Sheet sheet, int rowNum, int colNum) {
        String format = "INDIRECT($" + offset + (rowNum) + ")";
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        int firstRow = rowNum - 1;
        int lastRow = rowNum - 1;
        int firstCol = colNum;
        int lastCol = colNum;
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                lastRow, firstCol, lastCol);
        // 数据有效性对象

        DataValidation dataValidation = null;
        if (sheet instanceof XSSFSheet || sheet instanceof SXSSFSheet) {
            DataValidationHelper dvHelper = sheet.getDataValidationHelper();
            DataValidationConstraint constraint = dvHelper.createFormulaListConstraint(format);
            dataValidation = dvHelper.createValidation(constraint, regions);
        } else {
            DataValidationConstraint constraint = DVConstraint.createFormulaListConstraint(format);
            dataValidation = new HSSFDataValidation(regions, constraint);
        }

        // 适配xls和xlsx
        if (dataValidation instanceof HSSFDataValidation) {
            dataValidation.setSuppressDropDownArrow(false);
        } else {
            dataValidation.setSuppressDropDownArrow(true);
            dataValidation.setShowErrorBox(true);
        }

        dataValidation.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        sheet.addValidationData(dataValidation);
    }


    private static String getRange(int offset, int rowId, int colCount) {
        char start = (char) ('A' + offset);
        if (colCount <= 25) {
            char end = (char) (start + colCount - 1);
            return "$" + start + "$" + rowId + ":$" + end + "$" + rowId;
        } else {
            char endPrefix = 'A';
            char endSuffix = 'A';
            if ((colCount - 25) / 26 == 0 || colCount == 51) {// 26-51之间，包括边界（仅两次字母表计算）
                if ((colCount - 25) % 26 == 0) {// 边界值
                    endSuffix = (char) ('A' + 25);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                }
            } else {// 51以上
                if ((colCount - 25) % 26 == 0) {
                    endSuffix = (char) ('A' + 25);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26 - 1);
                } else {
                    endSuffix = (char) ('A' + (colCount - 25) % 26 - 1);
                    endPrefix = (char) (endPrefix + (colCount - 25) / 26);
                }
            }
            return "$" + start + "$" + rowId + ":$" + endPrefix + endSuffix + "$" + rowId;
        }
    }

    /**
     * 下拉的数据
     *
     * @return
     */
    public abstract List<CellValidation> getCellValidation();

    @Getter
    @Setter
    @ToString
    public static class CellValidation {
        /**
         * 下拉类型：10-单独下拉；20-级联下拉
         */
        private int type = 10;
        /**
         * 起始行(下标0开始)
         */
        private int firstRow;

        /**
         * lastRow  终止行，最大65535
         */
        private int lastRow = 65535;

        /**
         * 起始列 (下标0开始)
         */
        private int firstCol;

        /**
         * 终止列,一般情况下与firstCol值相同
         */
        private int lastCol;

        /**
         * 下拉值
         */
        private List<String> selectList;

        /**
         * 级联下拉值
         */
        private Map<String, List<String>> stringListMap;
    }
}
