package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Copyrights EnergyICT
 * Date: 11/05/11
 * Time: 10:24
 */
public class CeweDateFormats {

    private final TimeZone timeZone;

    /**
     * yyyyMMdd,HHmmss
     */
    private SimpleDateFormat queryDateFormat;

    /**
     * yyMMddHHmm
     */
    private SimpleDateFormat shortDateFormat;

    /**
     * yyMMddHHmmss
     */
    private SimpleDateFormat eventDateFormat;

    /**
     * yyyyMMddHHmmss
     */
    private SimpleDateFormat longDateFormat;

    public CeweDateFormats(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * Date format: yyyyMMdd,HHmmss
     */
    public SimpleDateFormat getQueryDateFormat() {
        if (queryDateFormat == null) {
            queryDateFormat = new SimpleDateFormat("yyyyMMdd,HHmmss");
            queryDateFormat.setTimeZone(getTimeZone());
        }
        return queryDateFormat;
    }

    /**
     * Date format: yyMMddHHmm
     */
    public SimpleDateFormat getShortDateFormat() {
        if (shortDateFormat == null) {
            shortDateFormat = new SimpleDateFormat("yyMMddHHmm");
            shortDateFormat.setTimeZone(getTimeZone());
        }
        return shortDateFormat;
    }

    /**
     * Date format: yyMMddHHmmss
     */
    public SimpleDateFormat getEventDateFormat() {
        if (eventDateFormat == null) {
            eventDateFormat = new SimpleDateFormat("yyMMddHHmmss");
            eventDateFormat.setTimeZone(getTimeZone());
        }
        return eventDateFormat;
    }

    /**
     * Date format: yyyyMMddHHmmss
     */
    public SimpleDateFormat getLongDateFormat() {
        if (longDateFormat == null) {
            longDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
            longDateFormat.setTimeZone(getTimeZone());
        }
        return longDateFormat;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
