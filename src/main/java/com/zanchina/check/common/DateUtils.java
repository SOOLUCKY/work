package com.zanchina.check.common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.poi.ss.usermodel.DateUtil;

/**
 * 日期工具类, 继承org.apache.commons.lang.time.DateUtils类
 *
 * @author jlin
 */
public class DateUtils extends org.apache.commons.lang3.time.DateUtils {

    public static final String yyyyMM = "yyyy-MM";
    public static final String yyyyMM1 = "yyyy/MM";
    public static final String yyyyMM2 = "yyyy.MM";
    public static final String yyyyMMdd = "yyyy-MM-dd";
    public static final String yyyyMMdd1 = "yyyy/MM/dd";
    public static final String yyyyMMdd2 = "yyyy.MM.dd";
    public static final String yyyyMMddHHmm = "yyyy-MM-dd HH:mm";
    public static final String yyyyMMddHHmm1 = "yyyy/MM/dd HH:mm";
    public static final String yyyyMMddHHmm2 = "yyyy.MM.dd HH:mm";
    public static final String yyyyMMddHHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String yyyyMMddHHmmss1 = "yyyy/MM/dd HH:mm:ss";
    public static final String yyyyMMddHHmmss2 = "yyyy.MM.dd HH:mm:ss";
    public static final String HHmm = "HH:mm";

    public static String[] parsePatterns = {
        "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
        "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
        "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd）
     */
    public static String getDate() {
        return getDate("yyyy-MM-dd");
    }

    /**
     * 得到当前日期字符串 格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String getDate(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }

    /**
     * 得到日期字符串 默认格式（yyyy-MM-dd） pattern可以为："yyyy-MM-dd" "HH:mm:ss" "E"
     */
    public static String formatDate(Date date, Object... pattern) {
        String formatDate = null;
        if (pattern != null && pattern.length > 0) {
            formatDate = DateFormatUtils.format(date, pattern[0].toString());
        } else {
            formatDate = DateFormatUtils.format(date, "yyyy-MM-dd");
        }
        return formatDate;
    }

    /**
     * 得到日期时间字符串，转换格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        return formatDate(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String getHHmmTime(Date date) {
        return formatDate(date, "HH:mm");
    }

    /**
     * 得到当前时间字符串 格式（HH:mm:ss）
     */
    public static String getTime() {
        return formatDate(new Date(), "HH:mm:ss");
    }

    /**
     * 得到当前日期和时间字符串 格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String getDateTime() {
        return formatDate(new Date(), "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 得到当前年份字符串 格式（yyyy）
     */
    public static String getYear() {
        return formatDate(new Date(), "yyyy");
    }

    /**
     * 得到当前月份字符串 格式（MM）
     */
    public static String getMonth() {
        return formatDate(new Date(), "MM");
    }

    /**
     * 得到当天字符串 格式（dd）
     */
    public static String getDay() {
        return formatDate(new Date(), "dd");
    }


    public static String[] WEEK_DAYS_ZJ = {"星期天", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};

    /**
     * 根据日期获取星期几
     */
    public static String getWeekday(Date d) {
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        return WEEK_DAYS_ZJ[c.get(Calendar.DAY_OF_WEEK) - 1];
    }


    /**
     * 日期型字符串转化为日期 格式 { "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss",
     * "yyyy/MM/dd HH:mm", "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm" }
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        try {
            return parseDate(str.toString(), parsePatterns);
        } catch (ParseException e) {
            return null;
        }
    }


    /**
     * 获取两个日期之间的天数
     */
    public static double getDistanceOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60 * 24);
    }

    /**
     * @param @return 当天零时刻
     * @return Date
     * @Description: 获取当天零点时刻
     * @author lyl <liuyuanlong@liangyibang.com> 2016年5月30日 下午4:50:27
     */
    public static Date getTodayTimeOfZero(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * @param @return 59分59秒
     * @return Date
     * @Description: 获取当天十二点时刻
     * @author lyl <liuyuanlong@liangyibang.com> 2016年5月30日 下午8:02:35
     */
    public static Date getTodayTimeOfTwelve(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取当前是周几（数字形式）
     */
    public static String getWeekOfDateDigit(Date dt) {
        String[] weekDays = {"7", "1", "2", "3", "4", "5", "6"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0) {
            w = 0;
        }
        return weekDays[w];
    }

    public static Date getMorning(Date d) {
        Calendar cal = Calendar.getInstance();
        if (null != d) {
            cal.setTime(d);
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static Date getNight(Date d) {
        Calendar cal = Calendar.getInstance();
        if (null != d) {
            cal.setTime(d);
        }
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public static String format(Date d, String f) {
        SimpleDateFormat sdf = new SimpleDateFormat(f);
        return sdf.format(d);
    }

    public static Date localDateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDate dateToLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }


    public static Date getDateAfterSecond(Date date, int second) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.SECOND, second);
        Date _date = calendar.getTime();
        return _date;
    }

    public static Date getBirthday(int age) {
        if (age <= 0 || age > 100) {
            return Calendar.getInstance().getTime();
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -age);
        return calendar.getTime();
    }

    public static Date getDay(Date d, int after) {
        Calendar cal = Calendar.getInstance();
        if (null != d) {
            cal.setTime(d);
        }
        cal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DATE) + after);
        return cal.getTime();
    }

    public static boolean inSameDay(Date date1, Date Date2) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date1);
        int year1 = calendar.get(Calendar.YEAR);
        int day1 = calendar.get(Calendar.DAY_OF_YEAR);

        calendar.setTime(Date2);
        int year2 = calendar.get(Calendar.YEAR);
        int day2 = calendar.get(Calendar.DAY_OF_YEAR);

        if ((year1 == year2) && (day1 == day2)) {
            return true;
        }
        return false;
    }

    /**
     * 获取某天的某个时刻
     *
     * @param d 某天
     * @param hour 设定的小时
     * @param min 设定的分钟
     */
    public static Date getDateTime(Date d, Integer hour, Integer min) {
        Calendar cal = Calendar.getInstance();
        if (null != d) {
            cal.setTime(d);
        }
        if (null == hour) {
            hour = 0;
        }
        cal.set(Calendar.HOUR_OF_DAY, hour);
        if (null == min) {
            min = 0;
        }
        cal.set(Calendar.MINUTE, min);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    /**
     * 获取两个日期之间的小时数
     */
    public static double getHourOfTwoDate(Date before, Date after) {
        long beforeTime = before.getTime();
        long afterTime = after.getTime();
        return (afterTime - beforeTime) / (1000 * 60 * 60);
    }

    /**
     * 获取指定月所有日期
     */
    public static List<Date> getAllDatesOfMonth(Date date) {

        int year = 2018;
        Month month = Month.of(Integer.valueOf(format(date, "MM")));

        List<Date> dates = IntStream.rangeClosed(1, YearMonth.of(year, month).lengthOfMonth())
            .mapToObj(day -> localDateToDate(LocalDate.of(year, month, day))).collect(Collectors.toList());
        return dates;
    }


}
