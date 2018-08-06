package com.zanchina.check;

import com.zanchina.check.common.DateUtils;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CheckApplicationTests {

    @Test
    public void contextLoads() throws ParseException {

//        Date d1 = DateUtils.parseDate("9:00", DateUtils.HHmm);
//        Date d2 = DateUtils.parseDate("2018/06/19 9:00", DateUtils.yyyyMMddHHmm1);
//
//        List<String> allDatesOfTwoDate = DateUtils.getAllDatesOfTwoDate("2018-06-30", "2018-07-15");
//
//        Date date = DateUtils.parseDate("2018-07-31".concat(" ").concat("08:55"), DateUtils.yyyyMMddHHmm);
//
//        System.out.print(date);

        List<String> strings = new ArrayList<>();
        strings.add("b1");
        strings.add("c");
        strings.add("a1");
        strings.add("b1");
        strings.add("a1");
        strings.add("b2");

        strings.add("a2");
        System.out.print(strings);
        strings.sort(Comparator.comparing(s -> s));
        System.out.print(strings);

    }

}
