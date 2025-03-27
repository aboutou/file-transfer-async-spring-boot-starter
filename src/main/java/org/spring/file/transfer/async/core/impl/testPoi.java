
package org.spring.file.transfer.async.core.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFDataValidation;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class testPoi {

    public static void main(String[] args) {
        Workbook workbook=new XSSFWorkbook();
        Cascade(workbook);
    }

    public static void Cascade(Workbook workbook) {
        // 创建一个excel


        // 创建需要用户填写的sheet
        XSSFSheet sheet = (XSSFSheet) workbook.createSheet("省市县");
        Row row0 = sheet.createRow(0);
        row0.createCell(5).setCellValue("渠道类型");
        row0.createCell(6).setCellValue("渠道来源");

        //得到第一级省名称，放在列表里
        String[] provinceArr = {"线上", "线下"};
        //依次列出各省的市、各市的县
        String[] cityJiangSu = {"招标网站", "营销自动化ACRM", "门户网络媒体", "移动社交媒体", "移动社交媒体", "搜索引擎", "平面媒体", "户外媒体", "其他"};
        String[] cityAnHui = {"行业博览会", "行业峰会/论坛/沙龙", "产业级市场活动", "大区/公路港市场活动", "本部门市场活动", "存量客户上下游", "政府关系网络", "个人关系网络", "传化生态业务", "其他"};
        //将有子区域的父区域放到一个数组中
        String[] areaFatherNameArr = {"线上", "线下"};
        Map<String, String[]> areaMap = new HashMap<String, String[]>();
        areaMap.put("线上", cityJiangSu);
        areaMap.put("线下", cityAnHui);

        //创建一个专门用来存放地区信息的隐藏sheet页
        //因此也不能在现实页之前创建，否则无法隐藏。
        Sheet hideSheet = workbook.createSheet("area");
        //这一行作用是将此sheet隐藏，功能未完成时注释此行,可以查看隐藏sheet中信息是否正确
//        workbook.setSheetHidden(workbook.getSheetIndex(hideSheet), true);

        int rowId = 0;
        // 设置第一行，存省的信息
        Row provinceRow = hideSheet.createRow(rowId++);
        provinceRow.createCell(0).setCellValue("渠道");
        for (int i = 0; i < provinceArr.length; i++) {
            Cell provinceCell = provinceRow.createCell(i + 1);
            provinceCell.setCellValue(provinceArr[i]);
        }
        // 将具体的数据写入到每一行中，行开头为父级区域，后面是子区域。
        for (int i = 0; i < areaFatherNameArr.length; i++) {
            String key = areaFatherNameArr[i];
            String[] son = areaMap.get(key);
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
            String formula = "area!" + range;
            name.setRefersToFormula(formula);
        }

        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper((XSSFSheet) sheet);
        // 省规则
        DataValidationConstraint provConstraint = dvHelper.createExplicitListConstraint(provinceArr);
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList provRangeAddressList = new CellRangeAddressList(2, 202, 5, 5);
        DataValidation provinceDataValidation = dvHelper.createValidation(provConstraint, provRangeAddressList);
        //验证
        provinceDataValidation.createErrorBox("error", "请选择正确的渠道");
        provinceDataValidation.setShowErrorBox(true);
        provinceDataValidation.setSuppressDropDownArrow(true);
        sheet.addValidationData(provinceDataValidation);

        //对前20行设置有效性
        for (int i = 3; i < 204; i++) {
            setDataValidation("F", sheet, i, 7);
        }
        FileOutputStream os = null;
        try {
            os =  new FileOutputStream(new File(System.getProperty("user.home"), "/Downloads/abc5555.xlsx"));
            workbook.write(os);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(os);
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
    public static void setDataValidation(String offset, XSSFSheet sheet, int rowNum, int colNum) {
        XSSFDataValidationHelper dvHelper = new XSSFDataValidationHelper(sheet);
        DataValidation data_validation_list;
        data_validation_list = getDataValidationByFormula(
                "INDIRECT($" + offset + (rowNum) + ")", rowNum, colNum, dvHelper);
        sheet.addValidationData(data_validation_list);
    }

    /**
     * 加载下拉列表内容
     *
     * @param formulaString
     * @param naturalRowIndex
     * @param naturalColumnIndex
     * @param dvHelper
     * @return
     */
    private static DataValidation getDataValidationByFormula(
            String formulaString, int naturalRowIndex, int naturalColumnIndex, XSSFDataValidationHelper dvHelper) {
        // 加载下拉列表内容
        // 举例：若formulaString = "INDIRECT($A$2)" 表示规则数据会从名称管理器中获取key与单元格 A2 值相同的数据，
        //如果A2是江苏省，那么此处就是江苏省下的市信息。
        XSSFDataValidationConstraint dvConstraint = (XSSFDataValidationConstraint) dvHelper.createFormulaListConstraint(formulaString);
        // 设置数据有效性加载在哪个单元格上。
        // 四个参数分别是：起始行、终止行、起始列、终止列
        int firstRow = naturalRowIndex - 1;
        int lastRow = naturalRowIndex - 1;
        int firstCol = naturalColumnIndex - 1;
        int lastCol = naturalColumnIndex - 1;
        CellRangeAddressList regions = new CellRangeAddressList(firstRow,
                lastRow, firstCol, lastCol);
        // 数据有效性对象
        // 绑定
        XSSFDataValidation data_validation_list = (XSSFDataValidation) dvHelper.createValidation(dvConstraint, regions);
        data_validation_list.setEmptyCellAllowed(false);
        if (data_validation_list instanceof XSSFDataValidation) {
            data_validation_list.setSuppressDropDownArrow(true);
            data_validation_list.setShowErrorBox(true);
        } else {
            data_validation_list.setSuppressDropDownArrow(false);
        }
        // 设置输入信息提示信息
        data_validation_list.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        // 设置输入错误提示信息
        //data_validation_list.createErrorBox("选择错误提示", "你输入的值未在备选列表中，请下拉选择合适的值！");
        return data_validation_list;
    }

    /**
     * 计算formula
     *
     * @param offset   偏移量，如果给0，表示从A列开始，1，就是从B列
     * @param rowId    第几行
     * @param colCount 一共多少列
     * @return 如果给入参 1,1,10. 表示从B1-K1。最终返回 $B$1:$K$1
     */
    public static String getRange(int offset, int rowId, int colCount) {
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
}
