package com.excel.test;

import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.usermodel.*;

import java.io.File;
import java.util.List;

public class ImgTest {

    public static void main(String[] args) throws Exception {


        String path = "C:\\Users\\torres.wu\\Downloads\\";
        OPCPackage opcPackage = OPCPackage.open(path + "QDM533-CN-1000PCS&2010pcs-SMT试产总结报告.xlsx");

        XSSFWorkbook workbook = new XSSFWorkbook(opcPackage);


        XSSFSheet sheet = workbook.getSheet("Issue Summary问题总结");

        XSSFDrawing drawing = sheet.getDrawingPatriarch();


        List<XSSFShape> shapes = drawing.getShapes();

        int i = 0;
        for (XSSFShape shape : shapes) {
            XSSFClientAnchor anchor = (XSSFClientAnchor) shape.getAnchor();
            short col1 = anchor.getCol1();
            int row1 = anchor.getRow1();
            String x = row1 + "行," + col1 + "列";
            System.out.println(x);

            if (shape instanceof XSSFPicture) {
                XSSFPicture pic = (XSSFPicture) shape;

                XSSFPictureData pictureData = pic.getPictureData();
                byte[] data = pictureData.getData();

                FileUtils.writeByteArrayToFile(new File(path, "aa\\" + x + "--" + (i++) + "." + pictureData.suggestFileExtension()), data);
            }

        }
    }
}
