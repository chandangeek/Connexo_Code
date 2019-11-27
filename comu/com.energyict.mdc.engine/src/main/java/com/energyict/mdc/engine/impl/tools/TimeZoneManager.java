/*
 * TimeZoneManager.java
 *
 * Created on 11 februari 2003, 14:19
 */

package com.energyict.mdc.engine.impl.tools;

import java.util.*;

/**
 * TimeZoneManager implements a TimeZone cache,
 * since <CODE>TimeZone.getTimeZone(String ID)</CODE> is
 * very slow.
 *
 * @author Karel
 */
public class TimeZoneManager {

    final private static Map timeZones = new HashMap();

    final private static String[] EXTRA = new String[]{
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
    static public synchronized TimeZone getTimeZone(String id) {
        TimeZone result = (TimeZone) timeZones.get(id);
        if (result == null) {
            result = TimeZoneManager.doGetTimeZone(id);
            timeZones.put(id, result);
        }
        return result;
    }

    static public TimeZone doGetTimeZone(String id) {
        for (int i = 0; i < EXTRA.length; i++) {
            if (EXTRA[i].equals(id)) {
                return doGetMyTimeZone(i);
            }
        }
        return TimeZone.getTimeZone(id);
    }

    static public TimeZone doGetMyTimeZone(int i) {
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
    static public TimeZone getTimeZone(int offset) {
        return getTimeZone(gmtId(offset));
    }

    static private String gmtId(int offset) {
        StringBuffer idBuffer = new StringBuffer("GMT");
        if (offset == 0) {
            return idBuffer.toString();
        }
        if (offset > 0) {
            idBuffer.append("+");
        }
        idBuffer.append(offset);
        return idBuffer.toString();
    }

    static public String[] getAvailableIDs() {
        List<String> result = new ArrayList<String>(Arrays.asList(TimeZone.getAvailableIDs()));
        result.addAll(Arrays.asList(EXTRA));
        for (int i = 1; i < 13; i++) {
            result.add("GMT+" + i);
            result.add("GMT" + (-i));
        }
        return result.toArray(new String[result.size()]);
    }
}
