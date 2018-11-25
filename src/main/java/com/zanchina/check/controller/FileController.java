package com.zanchina.check.controller;

import com.zanchina.check.common.ExcelData;
import com.zanchina.check.common.ExcelRead;
import com.zanchina.check.domain.Staff;
import com.zanchina.check.service.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

/**
 * Created by xiechunlu on 2018-06-15 下午2:40
 */
@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("")
    public String index() {
        return "redirect:/index.html";
    }

    @PostMapping("upload")
    @ResponseBody
    public ResponseEntity<byte[]> uploadAndExport(
        HttpServletRequest request, HttpServletResponse response) {

        try {
            ExcelRead inst = ExcelRead.getInst();

            MultipartFile insideFile = ((MultipartHttpServletRequest) request).getFile("inside");
            MultipartFile outFile = ((MultipartHttpServletRequest) request).getFile("outside");
            //补卡
            MultipartFile repairFile = ((MultipartHttpServletRequest) request).getFile("repair");
            //请假
            MultipartFile leaveFile = ((MultipartHttpServletRequest) request).getFile("leave");
            //加班
            MultipartFile overtimeFile = ((MultipartHttpServletRequest) request).getFile("overtime");

            List<Staff> staffList = new ArrayList<>();
            Map<String, Set<String>> repairMap = new HashMap<>();
            Map<String, Set<String>> leaveMap = new HashMap<>();
            Map<String, Set<String>> overtimeMap = new HashMap<>();

            InputStream inputStream;
            String fileName;
            ExcelData data;
            List<Staff> staffs;

            try {

                if (Objects.nonNull(insideFile) && StringUtils.isNotEmpty(insideFile.getOriginalFilename())) {
                    inputStream = insideFile.getInputStream();
                    fileName = insideFile.getOriginalFilename();

                    data = inst.parse(inputStream, fileName, false, 2);
                    staffs = fileService.wechatWorkInStatistics(data);
                    staffList.addAll(staffs);
                }
                if (Objects.nonNull(outFile) && StringUtils.isNotEmpty(outFile.getOriginalFilename())) {
                    inputStream = outFile.getInputStream();
                    fileName = outFile.getOriginalFilename();

                    data = inst.parse(inputStream, fileName, false, 2);
                    staffs = fileService.wechatWorkOutStatistics(data);
                    staffList.addAll(staffs);
                }
                if (Objects.nonNull(repairFile) && StringUtils.isNotEmpty(repairFile.getOriginalFilename())) {
                    inputStream = repairFile.getInputStream();
                    fileName = repairFile.getOriginalFilename();

                    data = inst.parse(inputStream, fileName, false, 0);
                    repairMap = fileService.repairRecord(data);
                }
                if (Objects.nonNull(leaveFile) && StringUtils.isNotEmpty(leaveFile.getOriginalFilename())) {
                    inputStream = leaveFile.getInputStream();
                    fileName = leaveFile.getOriginalFilename();

                    data = inst.parse(inputStream, fileName, false, 0);
                    leaveMap = fileService.leaveRecord(data);
                }
                if (Objects.nonNull(overtimeFile) && StringUtils
                    .isNotEmpty(overtimeFile.getOriginalFilename())) {
                    inputStream = overtimeFile.getInputStream();
                    fileName = overtimeFile.getOriginalFilename();

                    data = inst.parse(inputStream, fileName, false, 0);
                    overtimeMap = fileService.overtimeRecord(data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Staff> staffList1 = fileService.staffListCollect(staffList);
            return fileService.checkExport(staffList1, overtimeMap, repairMap, leaveMap);

        } catch (Exception e) {
            try {
                e.printStackTrace();
                response.getWriter().print(e.getMessage());
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            e.printStackTrace();
        }

        return null;
    }

}
