package com.zanchina.check.service.impl;

import com.zanchina.check.common.DateUtils;
import com.zanchina.check.common.ExcelData;
import com.zanchina.check.common.ExcelWrite;
import com.zanchina.check.domain.Staff;
import com.zanchina.check.domain.WorkCheck;
import com.zanchina.check.service.FileService;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.crypto.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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

    /**
     * 企业微信外出打卡统计
     */
    @Override
    public List<Staff> wechatWorkOutStatistics(ExcelData data) {

        //1. 根据员工姓名分组
        Map<String, List<String[]>> map = data.getDatas().stream().collect(Collectors.groupingBy(d -> d[1]));

        //2. 构造数据对象
        List<Staff> staffList = new ArrayList<>();

        map.entrySet().forEach(entry -> {
            Staff staff = new Staff();
            staff.setName(entry.getKey());

            entry.getValue().forEach(col -> {
                staff.setDepartment(col[3]);

                WorkCheck workCheck = new WorkCheck();
                workCheck.setDate(col[0]);
                workCheck.setOnTime(col[0].concat(" ").concat(col[4]));
                workCheck.setOffTime(col[0].concat(" ").concat(col[5]));
                workCheck.setState(caculateWorkState(workCheck));

                staff.getWorkCheckList().add(workCheck);
            });

            staffList.add(staff);

        });

        return staffList;
    }

    /**
     * 企业微信上下班打卡统计
     */
    @Override
    public List<Staff> wechatWorkInStatistics(ExcelData data) {
        //1. 根据员工姓名分组
        Map<String, List<String[]>> map = data.getDatas().stream()
            .filter(d -> !d[11].contains("--") || !d[14].contains("--")).collect(Collectors.groupingBy(d -> d[1]));

        //2. 构造数据对象
        List<Staff> staffList = new ArrayList<>();

        map.entrySet().forEach(entry -> {
            Staff staff = new Staff();
            staff.setName(entry.getKey());

            entry.getValue().forEach(col -> {
                staff.setDepartment(col[3]);

                WorkCheck workCheck = new WorkCheck();
                workCheck.setDate(col[0]);
                if (col[11].contains("--")) {
                    workCheck.setOnTime(col[0].concat(" ").concat(col[14]));
                } else {
                    workCheck.setOnTime(col[0].concat(" ").concat(col[11]));
                }

                if (col[14].contains("--")) {
                    workCheck.setOffTime(col[0].concat(" ").concat(col[11]));
                } else {
                    workCheck.setOffTime(col[0].concat(" ").concat(col[14]));
                }
                workCheck.setState(caculateWorkState(workCheck));
                workCheck.setApprove(col[7].contains("--") ? "" : col[7]);

                staff.getWorkCheckList().add(workCheck);
            });

            staffList.add(staff);

        });

        return staffList;
    }

    /**
     * 打卡机打卡记录统计
     */
    @Override
    public List<Staff> workRecord(ExcelData data) {
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
            staff.setId(entry.getKey());

            entry.getValue().entrySet().forEach(entry2 -> {

                staff.setName((entry2.getValue().get(0))[1]);

                List<Date> dateList = entry2.getValue().stream().map(e -> DateUtils.parseDate(e[3]))
                    .collect(Collectors.toList());

                WorkCheck workCheck = new WorkCheck();
                workCheck.setDate(DateUtils.formatDate(dateList.get(0), DateUtils.yyyyMMdd));

//                上班时间算一天当中最小的时间
                Date minDate = dateList.stream().min(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOnTime(DateUtils.formatDate(minDate, DateUtils.yyyyMMddHHmm));

                //下班时间算一天当中最大的时间
                Date maxDate = dateList.stream().max(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOffTime(DateUtils.formatDate(maxDate, DateUtils.yyyyMMddHHmm));
                workCheck.setState(caculateWorkState(workCheck));

                staff.getWorkCheckList().add(workCheck);

            });

            staff.getWorkCheckList().sort(Comparator.comparing(WorkCheck::getDate));
            staffList.add(staff);
        });

        staffList.sort(Comparator.comparing(Staff::getId));
        return staffList;
    }

    /**
     * 钉钉考勤记录处理
     */
    @Override
    public List<Staff> dingdingWorkStatistics(ExcelData data) {
        //1. 拿到数据后根据姓名和日期分组
        Map<String, Map<String, List<String[]>>> map = data.getDatas().stream()
            .collect(Collectors.groupingBy(d -> d[0], Collectors.groupingBy(
                d -> d[4])));

        //2. 取出最大时间和最小时间，计算考勤状态，生成workcheck
        List<Staff> staffList = new ArrayList<>();

        map.entrySet().forEach(entry -> {

            Staff staff = new Staff();
            staff.setName(entry.getKey());

            entry.getValue().entrySet().forEach(entry2 -> {

                List<Date> dateList = entry2.getValue().stream()
                    .map(e -> DateUtils.parseDate(e[4].concat(" ").concat(e[5])))
                    .collect(Collectors.toList());

                WorkCheck workCheck = new WorkCheck();
                workCheck.setDate(entry2.getKey());

//                上班时间算一天当中最小的时间
                Date minDate = dateList.stream().min(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOnTime(DateUtils.formatDate(minDate, DateUtils.yyyyMMddHHmm));

                //下班时间算一天当中最大的时间
                Date maxDate = dateList.stream().max(Comparator.comparing(d -> d.getTime())).get();
                workCheck.setOffTime(DateUtils.formatDate(maxDate, DateUtils.yyyyMMddHHmm));
                workCheck.setState(caculateWorkState(workCheck));

                staff.getWorkCheckList().add(workCheck);

            });

            staff.getWorkCheckList().sort(Comparator.comparing(WorkCheck::getDate));
            staffList.add(staff);
        });

        return staffList;
    }

    /**
     * 计算出勤状态（迟到、早退、迟到和早退、正常）
     */
    private String caculateWorkState(WorkCheck workCheck) {

        boolean isLate = false;
        boolean isEarly = false;
        String state = "正常";
        double hours = workCheck.getDuration();

        Date onTime = DateUtils.getDateTime(DateUtils.parseDate(workCheck.getDate()), 9, 30);
        Date offTime = DateUtils.getDateTime(DateUtils.parseDate(workCheck.getDate()), 18, 00);
        isLate = DateUtils.parseDate(workCheck.getOnTime()).after(onTime);
        isEarly = DateUtils.parseDate(workCheck.getOffTime()).before(offTime);

        //早上迟到
        if (isLate) {

            if (9 <= hours) {
                //工作时长大于九个小时
                state = "迟到";
            } else {
                state = "迟到和早退";
            }

        } else {
            //早上没有迟到
            if (isEarly || (!isEarly && 9.0 > hours)) {
                //早退了
                state = "早退";
            } else {
                state = "正常";
            }
        }

        return state;
    }

    /**
     * 汇总多张表
     */
    @Override
    public List<Staff> staffListCollect(List<Staff> staffList) {

        //1. 按照姓名分组
        Map<String, List<Staff>> collect = staffList.stream().collect(Collectors.groupingBy(Staff::getName));

        //2. 合并打卡数据
        List<Staff> newStaffList = new ArrayList<>();
        collect.entrySet().forEach(staffEntry -> {
            Staff staff = new Staff();
            staff.setName(staffEntry.getKey());

            List<Staff> value = staffEntry.getValue();

            List<WorkCheck> workChecks = new ArrayList<>();

            List<Staff> staffList1 = value.stream().filter(staff1 -> StringUtils.isNotBlank(staff1.getDepartment()))
                .collect(Collectors.toList());
            staff.setDepartment(staffList1.size() > 0 ? staffList1.get(0).getDepartment() : "");
            value.forEach(s -> {
                    workChecks.addAll(s.getWorkCheckList());
                }
            );

            //3. 通过日期分组，取各个时间最早和最晚的时间
            List<WorkCheck> newWorkCheckList = new ArrayList<>();
            workChecks.stream().collect(Collectors.groupingBy(WorkCheck::getDate)).entrySet().forEach(entry -> {

                List<WorkCheck> singleDateWorkCheck = entry.getValue();

                WorkCheck w = new WorkCheck();
                String onTime = singleDateWorkCheck.stream().min(Comparator
                    .comparing(workCheck -> DateUtils.parseDate(workCheck.getOnTime()))).get().getOnTime();
                w.setOnTime(onTime);
                String offTime = singleDateWorkCheck.stream().max(Comparator
                    .comparing(workCheck -> DateUtils.parseDate(workCheck.getOffTime()))).get().getOffTime();
                w.setOffTime(offTime);
                w.setDate(entry.getKey());
                w.setState(caculateWorkState(w));

                List<WorkCheck> approveList = singleDateWorkCheck.stream()
                    .filter(wc -> StringUtils.isNotBlank(wc.getApprove()))
                    .collect(Collectors.toList());
                w.setApprove(approveList.size() > 0 ? approveList.get(0).getApprove() : "");
                newWorkCheckList.add(w);
            });

            newWorkCheckList.sort(Comparator.comparing(WorkCheck::getDate));
            staff.setWorkCheckList(newWorkCheckList);
            newStaffList.add(staff);
        });

        newStaffList.sort(Comparator.comparing(Staff::getDepartment));
        return newStaffList;
    }

    @Override
    public ResponseEntity<byte[]> checkExport(List<Staff> staffList, Map<String, Set<String>> overtimeMap,
        Map<String, Set<String>> repairMap, Map<String, Set<String>> leaveMap)
        throws Exception {

        ExcelData data = new ExcelData();

        ArrayList<String> titleList = new ArrayList<>();
        titleList.add("姓名");

        // 取打卡记录中最早日期和最晚日期为标题的开始日期和结束日期
        String start = staffList.stream().min(
            Comparator.comparing(staff -> DateUtils.parseDate(staff.getWorkCheckList().stream()
                .min(Comparator.comparing(w -> DateUtils.parseDate(w.getDate()).getTime())).get().getDate())
                .getTime()))
            .get()
            .getWorkCheckList().get(0).getDate();

        List<WorkCheck> workCheckList = staffList.stream().max(
            Comparator.comparing(staff -> DateUtils.parseDate(staff.getWorkCheckList().stream()
                .max(Comparator.comparing(w -> DateUtils.parseDate(w.getDate()).getTime())).get().getDate())
                .getTime()))
            .get().getWorkCheckList();

        String end = workCheckList.get(workCheckList.size() - 1).getDate();

        List<String> dateStrs = DateUtils
            .getAllDatesOfTwoDate(DateUtils.format(DateUtils.parseDate(start), DateUtils.yyyyMMdd),
                DateUtils.format(DateUtils.parseDate(end), DateUtils.yyyyMMdd));

        titleList.addAll(dateStrs.stream().map(d ->
            d.concat("(")
                .concat(DateUtils.getWeekday(DateUtils.parseDate(d)))
                .concat(")")
        ).collect(Collectors.toList()));

        String[] titleStrArr = titleList.toArray(new String[titleList.size()]);
        data.setTitles(titleStrArr);

        List<String[]> datas = new ArrayList<>();

        staffList.forEach(staff -> {
            String[] d = new String[titleStrArr.length];

            //查看此人是否有加班数据
            Set<String> overtimeDate = overtimeMap.get(staff.getName());
            //查看此人是否有补打卡数据
            Set<String> repairDate = repairMap.get(staff.getName());
            //查看此人是否有请假数据
            Set<String> leaveDate = leaveMap.get(staff.getName());

            for (String title : titleList) {
                if ("姓名".equalsIgnoreCase(title)) {
                    d[0] = staff.getName().concat("(").concat(staff.getDepartment()).concat(")");
                } else {
                    int index = titleList.indexOf(title);
                    boolean isExist = false;
                    String week = DateUtils
                        .getWeekOfDateDigit(DateUtils.parseDate(title.substring(0, title.indexOf("("))));
                    boolean isWeekend = week.equalsIgnoreCase(Integer.toString(6)) || week
                        .equalsIgnoreCase(Integer.toString(7));

                    String today = title.substring(0, title.indexOf("("));
                    String detail = "";
                    Boolean isOverTime = false;
                    Boolean isRepair = false;
                    Boolean isleave = false;

                    // 判断当天是否有加班数据
                    if (Objects.nonNull(overtimeDate) && !overtimeDate.isEmpty()) {
                        List<String> overtime = overtimeDate.stream().filter(s -> s.equalsIgnoreCase(today))
                            .collect(Collectors.toList());

                        if (Objects.nonNull(overtime) && overtime.size() > 0) {
                            isOverTime = true;
                        }
                    }

                    // 判断当天是否有补打卡数据
                    if (Objects.nonNull(repairDate) && !repairDate.isEmpty()) {
                        List<String> repair = repairDate.stream().filter(s -> s.equalsIgnoreCase(today))
                            .collect(Collectors.toList());

                        if (Objects.nonNull(repair) && repair.size() > 0) {
                            isRepair = true;
                        }
                    }

                    // 判断当天是否有请假数据
                    if (Objects.nonNull(leaveDate) && !leaveDate.isEmpty()) {
                        List<String> leave = leaveDate.stream().filter(s -> s.equalsIgnoreCase(today))
                            .collect(Collectors.toList());

                        if (Objects.nonNull(leave) && leave.size() > 0) {
                            isleave = true;
                        }
                    }

                    if (isOverTime) {
                        detail = detail.concat("加班");
                    }

                    if (isleave) {
                        detail = detail.concat("请假");
                    }

                    if (isRepair) {
                        detail = detail.concat("补打卡");
                    }

                    for (WorkCheck check : staff.getWorkCheckList()) {

                        if (today.equalsIgnoreCase(
                            DateUtils.format(DateUtils.parseDate(check.getDate()), DateUtils.yyyyMMdd))) {

                            isExist = true;

                            if (!isWeekend) {
                                detail = detail.concat(check.getState().concat(","));
                            }



                            d[index] = detail.concat("(")
                                .concat(DateUtils.getHHmmTime(DateUtils.parseDate(check.getOnTime()))).concat("~")
                                .concat(
                                    DateUtils.getHHmmTime(DateUtils.parseDate(check.getOffTime())).concat(",小时数:")
                                        .concat(Double.toString(
                                            new BigDecimal(check.getDuration()).setScale(2, BigDecimal.ROUND_HALF_UP)
                                                .doubleValue()))
                                        .concat(")"));

                        }

                    }

                    if (!isExist) {
                        d[index] = detail;
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


    @Override
    public Map<String, Set<String>> overtimeRecord(ExcelData data) {

        //1. 根据员工姓名分组
        Map<String, List<String[]>> map = data.getDatas().stream().collect(Collectors.groupingBy(d -> d[2]));

        Map<String, Set<String>> overtimeData = new HashMap<>();

        map.entrySet().forEach(entry -> {
            String name = entry.getKey();
            List<String[]> value = entry.getValue();

            Set<String> dateList = new HashSet<>();

            value.stream()
                .forEach(strings1 -> {

                    try {
                        String start = DateUtils
                            .formatDate(DateUtils.parseDate(strings1[6], DateUtils.yyyyMMddHHmm1),
                                DateUtils.yyyyMMdd);
                        String end = DateUtils
                            .formatDate(DateUtils.parseDate(strings1[7], DateUtils.yyyyMMddHHmm1),
                                DateUtils.yyyyMMdd);
                        dateList.addAll(DateUtils.getAllDatesOfTwoDate(start, end));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            overtimeData.put(name, dateList);

        });

        return overtimeData;
    }

    @Override
    public Map<String, Set<String>> repairRecord(ExcelData data) {
        //1. 根据员工姓名分组
        Map<String, List<String[]>> map = data.getDatas().stream().collect(Collectors.groupingBy(d -> d[2]));

        Map<String, Set<String>> repairData = new HashMap<>();

        map.entrySet().forEach(entry -> {
            String name = entry.getKey();
            List<String[]> value = entry.getValue();

            Set<String> dateList = new HashSet<>();

            value.stream()
                .forEach(strings1 -> {

                    try {
                        String date = DateUtils
                            .formatDate(DateUtils.parseDate(strings1[6], DateUtils.yyyyMMddHHmm1),
                                DateUtils.yyyyMMdd);
                        dateList.add(date);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            repairData.put(name, dateList);

        });

        return repairData;
    }

    @Override
    public Map<String, Set<String>> leaveRecord(ExcelData data) {
        //1. 根据员工姓名分组
        Map<String, List<String[]>> map = data.getDatas().stream().collect(Collectors.groupingBy(d -> d[2]));

        Map<String, Set<String>> leaveData = new HashMap<>();

        map.entrySet().forEach(entry -> {
            String name = entry.getKey();
            List<String[]> value = entry.getValue();

            Set<String> dateList = new HashSet<>();

            value.stream()
                .forEach(strings1 -> {

                    try {
                        String start = DateUtils
                            .formatDate(DateUtils
                                    .parseDate(strings1[6].substring(0, strings1[6].lastIndexOf(" ")), DateUtils.yyyyMMdd1),
                                DateUtils.yyyyMMdd);
                        String end = DateUtils
                            .formatDate(DateUtils
                                    .parseDate(strings1[7].substring(0, strings1[7].lastIndexOf(" ")), DateUtils.yyyyMMdd1),
                                DateUtils.yyyyMMdd);
                        dateList.addAll(DateUtils.getAllDatesOfTwoDate(start, end));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            leaveData.put(name, dateList);

        });

        return leaveData;
    }
}
