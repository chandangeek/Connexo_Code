/*
 * TimeZoneManager.java
 *
 * Created on 11 februari 2003, 14:19
 */

package com.energyict.protocolimpl.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

/**
 * TimeZoneManager implements a TimeZone cache,
 * since <CODE>TimeZone.getTimeZone(String ID)</CODE> is
 * very slow.
 *
 * @author Karel
 */
public class TimeZoneManager {

    private static final Map<String, TimeZone> timeZones = new HashMap<>();

    private static final String[] EXTRA = new String[]{
            "2006/EST5EDT",
            "2006/CST6CDT",
            "2006/MST7MDT",
            "2006/PST8PDT"
    };

    private TimeZoneManager() {
    }

    /**
     * Gets the TimeZone for the given ID.
     *
     * @param id ID - the ID for a TimeZone, either an abbreviation such as "PST", a full name such as "America/Los_Angeles", or a custom ID such as "GMT-8:00". Note that the support of abbreviations is for JDK 1.1.x compatibility only and full names should be used.
     * @return the specified TimeZone, or the GMT zone if the given ID cannot be understood
     * @see TimeZone
     */
    public static synchronized TimeZone getTimeZone(String id) {
        TimeZone result = timeZones.get(id);
        if (result == null) {
            result = TimeZoneManager.doGetTimeZone(id);
            timeZones.put(id, result);
        }
        return result;
    }

    public static TimeZone doGetTimeZone(String id) {
        for (int i = 0; i < EXTRA.length; i++) {
            if (EXTRA[i].equals(id)) {
                return doGetMyTimeZone(i);
            }
        }
        return TimeZone.getTimeZone(id);
    }

    public static TimeZone doGetMyTimeZone(int i) {
        return new SimpleTimeZone(
                -3600 * 1000 * (5 + i),
                EXTRA[i],
                Calendar.APRIL, 1, -Calendar.SUNDAY, 7200000,
                Calendar.OCTOBER, -1, Calendar.SUNDAY, 7200000,
                3600000);
    }

    /**
     * Returns the timezone with the given offset from GMT
     *
     * @param offset offset
     * @return the timezone
     */
    public static TimeZone getTimeZone(int offset) {
        return getTimeZone(gmtId(offset));
    }

    private static String gmtId(int offset) {
        StringBuilder builder = new StringBuilder("GMT");
        if (offset == 0) {
            return builder.toString();
        }
        if (offset > 0) {
            builder.append("+");
        }
        builder.append(offset);
        return builder.toString();
    }

    public static String[] getAvailableIDs() {
        List<String> result = new ArrayList<>(Arrays.asList(TimeZone.getAvailableIDs()));
        result.addAll(Arrays.asList(EXTRA));
        for (int i = 1; i < 13; i++) {
            result.add("GMT+" + i);
            result.add("GMT" + (-i));
        }
        return result.toArray(new String[result.size()]);
    }
}
