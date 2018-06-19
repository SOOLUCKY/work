package com.zanchina.check.controller;

import com.zanchina.check.common.ExcelData;
import com.zanchina.check.common.ExcelRead;
import com.zanchina.check.service.FileService;
import java.io.InputStream;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
        @RequestParam MultipartFile file) {
        try {
            if (Objects.isNull(file)) {
                return null;
            }

            InputStream inputStream = file.getInputStream();
            ExcelRead inst = ExcelRead.getInst();
            ExcelData data = inst.parse(inputStream, file.getOriginalFilename(), false);

            return fileService.uploadAndExport(data);
        } catch (Exception e) {
            return null;
        }
    }

}
