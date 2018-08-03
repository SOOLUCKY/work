package com.zanchina.check.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * @author zhoubinglong
 */
@Slf4j
public class ExcelWrite {

    private static ExcelWrite inst = new ExcelWrite();

    private ExcelWrite() {
    }

    private String sheetName = "Sheet1";

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public static ExcelWrite getInst() {
        return inst;
    }

    public void write(ExcelData data, File out) {
        write(data, out, null);
    }

    public void write(ExcelData data, File out, File template) {
        Workbook workbook = null;
        Sheet sheet = null;
        if (null != template) {
            try {
                if (template.getName().contains(".") && template.getName()
                    .substring(template.getName().lastIndexOf(".") + 1, template.getName().length()).toLowerCase()
                    .equals("xls")) {
                    workbook = new HSSFWorkbook(new POIFSFileSystem(new FileInputStream(template)));
                } else {
                    workbook = new XSSFWorkbook(new FileInputStream(template));
                }
                sheet = workbook.getSheetAt(0);
                sheet.setColumnWidth(0, 3766);
            } catch (Exception e) {
            }
        } else {
            if (out.getName().contains(".") && out.getName()
                .substring(out.getName().lastIndexOf(".") + 1, out.getName().length()).toLowerCase().equals("xls")) {
                workbook = new HSSFWorkbook();
            } else {
                workbook = new XSSFWorkbook();
            }
            sheet = workbook.createSheet(sheetName);
        }
        if (null != data.getTitles() && data.getTitles().length > 0) {
            Row titleRow = sheet.createRow(0);
            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setFillForegroundColor((short) 13);
            cellStyle.setFillPattern(CellStyle.ALIGN_JUSTIFY);
            Font font = workbook.createFont();
            font.setBoldweight(Font.BOLDWEIGHT_BOLD);
            font.setFontHeightInPoints((short) 16);
            cellStyle.setFont(font);
            titleRow.setRowStyle(cellStyle);
            for (int j = 0; j < data.getTitles().length; j++) {
                Cell busiDataCell = titleRow.createCell(j);
                busiDataCell.setCellValue(data.getTitles()[j]);
            }
        }

        for (int i = 0; i < data.getDatas().size(); i++) {
            String[] arr = data.getDatas().get(i);
            Row busiDataRow = sheet.createRow(i + 1);
            for (int j = 0; j < arr.length; j++) {
                Cell busiDataCell = busiDataRow.createCell(j);
                busiDataCell.setCellValue(arr[j]);
            }
        }
        try {
            OutputStream os = new FileOutputStream(out);
            workbook.write(os);
            if (null != workbook) {
                workbook.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
