/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.validators.impl;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Utils {

    static BigDecimal BIG_DECIMAL_0 = bigDecimal(0D);
    static BigDecimal BIG_DECIMAL_1 = bigDecimal(1D);
    static BigDecimal BIG_DECIMAL_2 = bigDecimal(2D);
    static BigDecimal BIG_DECIMAL_3 = bigDecimal(3D);
    static BigDecimal BIG_DECIMAL_4 = bigDecimal(4D);
    static BigDecimal BIG_DECIMAL_10 = bigDecimal(10D);
    static BigDecimal BIG_DECIMAL_15 = bigDecimal(15D);
    static BigDecimal BIG_DECIMAL_20 = bigDecimal(20D);
    static BigDecimal BIG_DECIMAL_30 = bigDecimal(30D);
    static BigDecimal BIG_DECIMAL_50 = bigDecimal(50D);
    static BigDecimal BIG_DECIMAL_100 = bigDecimal(100D);
    static BigDecimal BIG_DECIMAL_130 = bigDecimal(130D);

    static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    static Instant INSTANT_2016_FEB_01 = instant("20160201000000");
    static Instant INSTANT_2016_FEB_02 = instant("20160202000000");
    static Instant INSTANT_2016_FEB_03 = instant("20160203000000");
    static Instant INSTANT_2016_FEB_04 = instant("20160204000000");
    static Instant INSTANT_2016_FEB_05 = instant("20160205000000");


    static BigDecimal bigDecimal(Double value) {
        return BigDecimal.valueOf(value);
    }

    static Instant instant(String value) {
        return LocalDate.from(DATE_FORMAT.parse(value)).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant();
    }
}
