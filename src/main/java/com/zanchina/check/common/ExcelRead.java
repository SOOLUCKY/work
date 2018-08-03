package com.zanchina.check.common;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Slf4j
public class ExcelRead {

    private static ExcelRead inst = new ExcelRead();

    private ExcelRead() {
    }

    public static ExcelRead getInst() {
        return inst;
    }

    public ExcelData parse(String fileName) {
        Workbook wb = null;

        try {
            InputStream is = new FileInputStream(fileName);
            String postfix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (postfix.equals(".xls")) {
                // 针对 2003 Excel 文件
                wb = new HSSFWorkbook(new POIFSFileSystem(is));
            } else {
                // 针对2007 Excel 文件
                wb = new XSSFWorkbook(is);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        Sheet sheet = wb.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();// 得到总行数
        Row row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();
        String titles[] = readExcelTitle(row);

        List<String[]> list = new ArrayList<String[]>();
        String[] content = null;
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++) {
            int j = 0;
            row = sheet.getRow(i);
            content = new String[colNum];
            do {
                content[j] = getCellFormatValue(row.getCell(j)).trim();
                j++;
            } while (j < colNum);
            list.add(content);
        }

        ExcelData data = new ExcelData();
        data.setDatas(list);
        data.setTitles(titles);
        return data;
    }


    public ExcelData parse(InputStream in, String fileName, boolean bool, Integer titleIndex) {
        Workbook wb = null;
        try {
            String postfix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
            if (postfix.equals(".xls")) {
                // 针对 2003 Excel 文件
                wb = new HSSFWorkbook(new POIFSFileSystem(in));
            } else {
                // 针对2007 Excel 文件
                wb = new XSSFWorkbook(in);
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

        Sheet sheet = wb.getSheetAt(0);
        int rowNum = sheet.getLastRowNum();// 得到总行数
        Row row = sheet.getRow(titleIndex);
        int colNum = row.getPhysicalNumberOfCells();
        String titles[] = readExcelTitle(row);

        List<String[]> list = new ArrayList<String[]>();
        String[] content = null;
        // 正文内容应该从第二行开始,第一行为表头的标题
        //bool是否从标题行开始
        if (bool) {
            for (int i = 0 + titleIndex; i <= rowNum; i++) {
                int j = 0;
                row = sheet.getRow(i);
                content = new String[colNum];
                do {
                    if (null != row.getCell(j)) {
                        if (null != getCellFormatValue(row.getCell(j))) {
                            content[j] = getCellFormatValue(row.getCell(j));
                        }
                    }
                    j++;
                } while (j < colNum);
                list.add(content);
            }
        } else {
            for (int i = 1 + titleIndex; i <= rowNum; i++) {
                int j = 0;
                row = sheet.getRow(i);
                content = new String[colNum];
                do {
                    content[j] = getCellFormatValue(row.getCell(j));
                    j++;
                } while (j < colNum);
                list.add(content);
            }
        }

        ExcelData data = new ExcelData();
        data.setDatas(list);
        data.setTitles(titles);
        return data;
    }

    private String[] readExcelTitle(Row row) {
        int colNum = row.getPhysicalNumberOfCells();// 获取行的列数
        String[] titles = new String[colNum];
        for (int i = 0; i < titles.length; i++) {
            titles[i] = getCellFormatValue(row.getCell(i));
        }
        return titles;
    }

    private String getCellFormatValue(Cell cell) {
        String cellvalue = "";
        DecimalFormat df = new DecimalFormat("#");
        if (cell != null) {
            // 判断当前Cell的Type
            switch (cell.getCellType()) {
                // 如果当前Cell的Type为NUMERIC
                case Cell.CELL_TYPE_NUMERIC: {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell)) {
                        // 方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.yyyyMMddHHmm1);
                        cellvalue = sdf.format(date);
                    } else {
                        // 如果是纯数字取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                // 如果当前Cell的Type为STRIN
                case Cell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                default:
                    cellvalue = " ";
            }
        }
        return cellvalue;
    }

}
