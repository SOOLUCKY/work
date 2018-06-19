package com.zanchina.check.service;

import com.zanchina.check.common.ExcelData;
import java.text.ParseException;
import org.springframework.http.ResponseEntity;

/**
 * Created by xiechunlu on 2018-06-15 下午2:41
 */

public interface FileService {

    ResponseEntity<byte[]> uploadAndExport(ExcelData data) throws Exception;
}
