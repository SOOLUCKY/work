package com.zanchina.check.service.impl;

import com.zanchina.check.common.DateUtils;
import com.zanchina.check.common.ExcelData;
import com.zanchina.check.common.ExcelWrite;
import com.zanchina.check.domain.Staff;
import com.zanchina.check.domain.WorkCheck;
import com.zanchina.check.service.FileService;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Created by xiechunlu on 2018-06-15 下午2:41
 */
@Service
@Slf4j
public class FileServiceImpl implements FileService {

    @Override
    public ResponseEntity<byte[]> uploadAndExport(ExcelData data) throws Exception {

        //1. 拿到数据后根据id和日期分组
        Map<String, Map<String, List<String[]>>> map = data.getDatas().stream()
            .collect(Collectors.groupingBy(d -> d[2], Collectors.groupingBy(
                d -> {
                    String date;
                    try {
                        date = DateUtils
                            .formatDate(DateUtils.parseDate(d[3], DateUtils.yyyyMMddHHmm1),
                                DateUtils.yyyyMMdd1);
                    } catch (Exception e) {
                        return null;
                    }

                    return date;
                })));

        //2. 取出最大时间和最小时间，计算考勤状态，生成workcheck
        List<Staff> staffList = new ArrayList<>();

        map.entrySet().forEach(entry -> {

            Staff staff = new Staff();
            staff.setId(Integer.valueOf(entry.getKey()));

            entry.getValue().entrySet().forEach(entry2 -> {

                staff.setName((entry2.getValue().get(0))[1]);

                List<Date> dateList = entry2.getValue().stream().map(e -> DateUtils.parseDate(e[3]))
                    .collect(Collectors.toList());

                WorkCheck workCheck = new WorkCheck();
                workCheck.setDate(DateUtils.formatDate(dateList.get(0), DateUtils.yyyyMMdd1));

//                上班时间算一天当中最小的时间
                Date minDate = dateList.stream().min(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOnTime(DateUtils.formatDate(minDate, DateUtils.yyyyMMddHHmm1));

                //下班时间算一天当中最大的时间
                Date maxDate = dateList.stream().max(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOffTime(DateUtils.formatDate(maxDate, DateUtils.yyyyMMddHHmm1));

                //计算出勤状态（迟到、早退、迟到和早退、正常）
                boolean isLate = false;
                boolean isEarly = false;
                double hours = workCheck.getDuration();

                Date onTime = DateUtils.getDateTime(dateList.get(0), 9, 30);
                Date offTime = DateUtils.getDateTime(dateList.get(0), 18, 00);
                isLate = DateUtils.parseDate(workCheck.getOnTime()).after(onTime);
                isEarly = DateUtils.parseDate(workCheck.getOffTime()).before(offTime);

                //早上迟到
                if (isLate) {

                    if (9 <= hours) {
                        //工作时长大于九个小时
                        workCheck.setState("迟到");
                    } else {
                        workCheck.setState("迟到和早退");
                    }

                } else {
                    //早上没有迟到
                    if (isEarly || (!isEarly && 9.0 > hours)) {
                        //早退了
                        workCheck.setState("早退");
                    } else {
                        workCheck.setState("正常");
                    }
                }

                staff.getWorkCheckList().add(workCheck);

            });

            staff.getWorkCheckList().sort(Comparator.comparing(WorkCheck::getDate));
            staffList.add(staff);
        });

        staffList.sort(Comparator.comparing(Staff::getId));
        return checkExport(staffList);
    }

    private ResponseEntity<byte[]> checkExport(List<Staff> staffList) throws Exception {

        ExcelData data = new ExcelData();

        ArrayList<String> titleList = new ArrayList<>();
        titleList.add("姓名");
        titleList.addAll(staffList.stream().max(Comparator.comparing(staff -> staff.getWorkCheckList().size())).get()
            .getWorkCheckList().stream().map(workCheck ->
                DateUtils.format(DateUtils.parseDate(workCheck.getDate()), DateUtils.yyyyMMdd1).concat("(")
                    .concat(DateUtils.getWeekday(DateUtils.parseDate(workCheck.getDate())))
                    .concat(")")
            ).collect(Collectors.toList()));

        String[] titleStrArr = titleList.toArray(new String[titleList.size()]);
        data.setTitles(titleStrArr);

        List<String[]> datas = new ArrayList<>();

        staffList.forEach(staff -> {
            String[] d = new String[titleStrArr.length];

            for (String title : titleList) {
                if ("姓名".equalsIgnoreCase(title)) {
                    d[0] = staff.getName();
                } else {
                    int index = titleList.indexOf(title);
                    boolean isExist = false;
                    String week = DateUtils
                        .getWeekOfDateDigit(DateUtils.parseDate(title.substring(0, title.indexOf("("))));
                    boolean isWeekend = week.equalsIgnoreCase(Integer.toString(6)) || week
                        .equalsIgnoreCase(Integer.toString(7));

                    for (WorkCheck check : staff.getWorkCheckList()) {
                        if (title.substring(0, title.indexOf("(")).equalsIgnoreCase(check.getDate())) {

                            String detail = "";
                            if (!isWeekend) {
                                detail = check.getState();
                            }

                            d[index] = detail.concat("(")
                                .concat(DateUtils.getHHmmTime(DateUtils.parseDate(check.getOnTime()))).concat("~")
                                .concat(
                                    DateUtils.getHHmmTime(DateUtils.parseDate(check.getOffTime())).concat(",小时数:")
                                        .concat(Double.toString(
                                            new BigDecimal(check.getDuration())
                                                .setScale(2, BigDecimal.ROUND_HALF_UP)
                                                .doubleValue()))
                                        .concat(")"));
                            isExist = true;
                        }
                    }

                    if (!isExist) {
                        if (!isWeekend) {
                            d[index] = "无考勤记录";
                        } else {
                            d[index] = "";
                        }
                    }
                }

            }

            datas.add(d);

        });

        data.setDatas(datas);
        ExcelWrite inst = ExcelWrite.getInst();
        File dst = File.createTempFile("export", ".xls");
        inst.write(data, dst);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        String dfileName = "考勤统计信息.xls";
        dfileName = new String(dfileName.getBytes("UTF-8"), "iso8859-1");
        headers.setContentDispositionFormData("attachment", dfileName);
        return new ResponseEntity<>(FileUtils.readFileToByteArray(dst), headers, HttpStatus.CREATED);
    }
}
