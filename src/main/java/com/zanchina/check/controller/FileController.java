package com.zanchina.check.controller;

import com.zanchina.check.common.ExcelData;
import com.zanchina.check.common.ExcelRead;
import com.zanchina.check.domain.Staff;
import com.zanchina.check.service.FileService;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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

            List<MultipartFile> files = ((MultipartHttpServletRequest) request)
                .getFiles("file");

            List<Staff> staffList = new ArrayList<>();

            files = files.stream().filter(f -> StringUtils.isNotBlank(f.getOriginalFilename()))
                .collect(Collectors.toList());
            files.forEach(file -> {
                try {

                    InputStream inputStream = file.getInputStream();
                    String fileName = file.getOriginalFilename();
                    ExcelData data = null;
                    List<Staff> staffs = new ArrayList<>(0);

                    if (fileName.contains("上下班打卡_日报")) {
                        data = inst.parse(inputStream, fileName, false, 2);
                        staffs = fileService.wechatWorkInStatistics(data);
                    } else if (fileName.contains("外出打卡_日报")) {
                        data = inst.parse(inputStream, fileName, false, 2);
                        staffs = fileService.wechatWorkOutStatistics(data);
                    } else if (fileName.contains("考勤导出")) {
                        data = inst.parse(inputStream, fileName, false, 0);
                        staffs = fileService.workRecord(data);
                    } else if (fileName.contains("钉钉")) {
                        data = inst.parse(inputStream, fileName, false, 2);
                        staffs = fileService.dingdingWorkStatistics(data);
                    }
                    staffList.addAll(staffs);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            List<Staff> staffList1 = fileService.staffListCollect(staffList);
            return fileService.checkExport(staffList1);

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
