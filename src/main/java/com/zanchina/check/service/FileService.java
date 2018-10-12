package com.zanchina.check.service;

import com.zanchina.check.common.ExcelData;
import com.zanchina.check.domain.Staff;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.crypto.Data;
import org.springframework.http.ResponseEntity;

/**
 * Created by xiechunlu on 2018-06-15 下午2:41
 */

public interface FileService {

    ResponseEntity<byte[]> checkExport(List<Staff> staffList, Map<String, Set<String>> map) throws Exception;

    List<Staff> wechatWorkOutStatistics(ExcelData data);

    List<Staff> wechatWorkInStatistics(ExcelData data);

    List<Staff> workRecord(ExcelData data);

    List<Staff> staffListCollect(List<Staff> staffList);

    List<Staff> dingdingWorkStatistics(ExcelData data);

    Map<String, Set<String>> overtimeRecord(ExcelData data);
}
