package com.zanchina.check;

import com.zanchina.check.common.DateUtils;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CheckApplicationTests {

    @Test
    public void contextLoads() throws ParseException {

        Date d1 = DateUtils.parseDate("9:00", DateUtils.HHmm);
        Date d2 = DateUtils.parseDate("2018/06/19 9:00", DateUtils.yyyyMMddHHmm1);

        List<String> allDatesOfTwoDate = DateUtils.getAllDatesOfTwoDate("2018-06-30", "2018-07-15");

        System.out.print(allDatesOfTwoDate);
    }

}
