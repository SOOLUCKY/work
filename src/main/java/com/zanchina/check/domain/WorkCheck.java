package com.zanchina.check.domain;

import com.zanchina.check.common.DateUtils;
import lombok.Data;

/**
 * Created by xiechunlu on 2018-06-15 下午4:17
 */
@Data
public class WorkCheck {


    /**
     * 出勤日期
     */
    private String date;

    /**
     * 上班签到时间
     */
    private String onTime;

    /**
     * 下班签到时间
     */
    private String offTime;

    /**
     * 出勤状态
     */
    private String state;

    /**
     * 审批
     */
    private String approve = "";

    /**
     * 出勤时长(小时为单位)
     */
    private Double duration;

    public Double getDuration() {
        return (double) (DateUtils.parseDate(getOffTime()).getTime() - DateUtils.parseDate(getOnTime()).getTime()) / (60
            * 60 * 1000) % 24;
    }

}
