package com.zanchina.check.common;

import java.util.List;

@lombok.Data
public class ExcelData {
    private String[] titles;
    private List<String[]> datas;
}
