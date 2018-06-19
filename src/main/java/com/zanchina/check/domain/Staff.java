package com.zanchina.check.domain;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * Created by xiechunlu on 2018-06-15 下午5:52
 */
@Data
public class Staff {

    /**
     * 考勤号
     */
    private Integer id;

    /**
     * 员工名
     */
    private String name;

    /**
     * 出勤列表
     */
    private List<WorkCheck> workCheckList = new ArrayList<>();
}
