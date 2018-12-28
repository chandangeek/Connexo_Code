/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tou.campaign;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ToUUtil {
    public static String parseNumberToDate(String string) {
        string = Instant.ofEpochMilli(Long.parseLong(string)).toString();
        List<String> str = new ArrayList<>();
        str.add(string.substring(8, 10));
        switch (string.substring(5, 7)) {
            case "01":
                str.add("Jan");
                break;
            case "02":
                str.add("Feb");
                break;
            case "03":
                str.add("Mar");
                break;
            case "04":
                str.add("Apr");
                break;
            case "05":
                str.add("May");
                break;
            case "06":
                str.add("Jun");
                break;
            case "07":
                str.add("Jul");
                break;
            case "08":
                str.add("Aug");
                break;
            case "09":
                str.add("Sep");
                break;
            case "10":
                str.add("Oct");
                break;
            case "11":
                str.add("Nov");
                break;
            case "12":
                str.add("Dec");
                break;
        }
        str.add("'" + string.substring(2, 4));
        str.add(string.substring(11, 16));
        return str.get(0) + " " + str.get(1) + " " + str.get(2) + " at " + str.get(3);
    }

    public static Instant getTooday(Clock clok){
        return Instant.parse(clok.instant().toString().substring(0,11)+"00:00:00Z");
    }

    public static Instant getTomorrow(Clock clok){
        return Instant.parse(clok.instant().toString().substring(0,8)+(Long.parseLong(clok.instant().toString().substring(8,10)))+"T00:00:00Z");
    }

    public static Instant parseStringToInstant(String text) {
        List<String> elementsOfDate = new ArrayList<>();
        List<String> elementsOfDate2 = new ArrayList<>();
        Arrays.asList(text.split(" ")).forEach(s -> elementsOfDate.add(s));
        elementsOfDate2.add("20" + elementsOfDate.get(2).substring(1));
        switch (elementsOfDate.get(1)) {
            case "Jan":
                elementsOfDate2.add("01");
                break;
            case "Feb":
                elementsOfDate2.add("02");
                break;
            case "Mar":
                elementsOfDate2.add("03");
                break;
            case "Apr":
                elementsOfDate2.add("04");
                break;
            case "May":
                elementsOfDate2.add("05");
                break;
            case "Jun":
                elementsOfDate2.add("06");
                break;
            case "Jul":
                elementsOfDate2.add("07");
                break;
            case "Aug":
                elementsOfDate2.add("08");
                break;
            case "Sep":
                elementsOfDate2.add("09");
                break;
            case "Oct":
                elementsOfDate2.add("10");
                break;
            case "Nov":
                elementsOfDate2.add("11");
                break;
            case "Dec":
                elementsOfDate2.add("12");
                break;
        }
        elementsOfDate2.add(elementsOfDate.get(0));
        elementsOfDate2.add(elementsOfDate.get(4));
        String dateTime = elementsOfDate2.get(0) + "-" + elementsOfDate2.get(1) + "-" + elementsOfDate2.get(2) + "T" + elementsOfDate2.get(3) + ":00Z";
        return Instant.parse(dateTime);
    }
}
