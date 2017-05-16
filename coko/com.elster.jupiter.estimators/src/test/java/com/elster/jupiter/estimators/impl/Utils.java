/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.estimators.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {

    static BigDecimal BIG_DECIMAL_100 = bigDecimal(100D);
    static BigDecimal BIG_DECIMAL_200 = bigDecimal(200D);
    static BigDecimal BIG_DECIMAL_300 = bigDecimal(300D);
    static BigDecimal BIG_DECIMAL_400 = bigDecimal(400D);
    static BigDecimal BIG_DECIMAL_500 = bigDecimal(500D);

    private static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static Instant INSTANT_2016_FEB_01 = instant("20160201000000");
    static Instant INSTANT_2016_FEB_02 = instant("20160202000000");
    static Instant INSTANT_2016_FEB_03 = instant("20160203000000");
    static Instant INSTANT_2016_FEB_04 = instant("20160204000000");
    static Instant INSTANT_2016_FEB_05 = instant("20160205000000");


    private static BigDecimal bigDecimal(Double value) {
        return BigDecimal.valueOf(value);
    }

    private static Instant instant(String value) {
        return LocalDate.from(DATE_FORMAT.parse(value)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }
}
