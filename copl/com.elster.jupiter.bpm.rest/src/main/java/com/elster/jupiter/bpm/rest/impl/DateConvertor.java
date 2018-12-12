/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DateConvertor {

    public static final String DATE_FORMAT = "dd/MM/yyyy HH:mm";
    public static final String DATE_FORMAT_SEC = "dd/MM/yyyy HH:mm:ss";

    public static String convertTimeStamps(String timeStamp, boolean hasMilisecond){
        String result = "-";
        Calendar cal = Calendar.getInstance();
        if (!timeStamp.equals("null")) {
            long time = Long.parseLong(timeStamp);
            cal.setTimeInMillis(time);
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
            if (hasMilisecond) {
                dateFormat = new SimpleDateFormat(DATE_FORMAT_SEC);
            }
            result = dateFormat.format(cal.getTime());
        }
        return result;
    }
}
