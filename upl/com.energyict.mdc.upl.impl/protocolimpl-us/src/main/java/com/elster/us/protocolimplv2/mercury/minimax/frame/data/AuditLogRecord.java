package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import com.elster.us.protocolimplv2.mercury.minimax.utility.UnitMapper;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Represents records received from the audit log (used for interval data)
 *
 * @author James Fox
 */
public class AuditLogRecord {

    public final static int ALARM_INDEX_SHUTDOWN = 1;
    public final static int ALARM_INDEX_FLOW_RATE_HIGH = 2;
    public final static int ALARM_INDEX_P1_HIGH = 3;
    public final static int ALARM_INDEX_T1_HIGH = 4;
    public final static int ALARM_INDEX_P1_LOW = 5;
    public final static int ALARM_INDEX_T1_LOW = 6;
    public final static int ALARM_INDEX_PULSERB_BACKUP = 8;
    public final static int ALARM_INDEX_PULSERA_BACKUP = 9;
    public final static int ALARM_INDEX_DAILY_COR_VOL_HIGH = 10;
    public final static int ALARM_INDEX_TEMP_OUT_OF_RANGE = 12;
    public final static int ALARM_INDEX_PRESS_OUT_OF_RANGE = 13;
    public final static int ALARM_INDEX_INTERNAL_FAULT = 14;
    public final static int ALARM_INDEX_SWITCH2_FAULT = 15;
    public final static int ALARM_INDEX_SWITCH1_FAULT = 16;
    public final static int ALARM_INDEX_BATT_CYCLES_HIGH = 18;
    public final static int ALARM_INDEX_BATT_LOW = 19;

    public final static String RECORD_TYPE_INTERVAL = "0";
    public final static String RECORD_TYPE_SERIAL = "3";
    public final static String RECORD_TYPE_CHANGE = "7";

    private Date date;
    private List<String> stuff = new ArrayList<String>();

    private boolean[] alarms;

    private Logger logger;

    public AuditLogRecord(Logger logger) {
        this.logger = logger;
    }

    public void setDate(String date) {
        if (date == null) {
            logger.severe("Cannot parse null date");
            throw new IllegalArgumentException("Cannot set date to null");
        } else {
            try {
                this.date = UnitMapper.getEventDateFormat().parse(date);
            } catch (ParseException pe) {
                logger.severe("Caught ParseException when trying to parse date " + date + ": " + pe);
                throw new IllegalArgumentException("Cannot parse date " + date);
            }
        }
    }

    public Date getTimestamp() {
        return date;
    }

    public void setTime(String time) {
        if (date == null) {
            throw new IllegalArgumentException("Date must be set before setting time");
        }
        if (time == null) {
            logger.severe("Cannot set null date");
            throw new IllegalArgumentException("Time string cannot be null");
        }
        if ("".equals(time)) {
            logger.severe("Cannot set date with empty string");
            throw new IllegalArgumentException("Time string cannot be empty");
        }

        try {
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.date);
            int hours = Integer.parseInt(time.substring(0, 2));
            int minutes = Integer.parseInt(time.substring(2, 4));
            int seconds = Integer.parseInt(time.substring(4, 6));
            cal.set(Calendar.HOUR_OF_DAY, hours);
            cal.set(Calendar.MINUTE, minutes);
            cal.set(Calendar.SECOND, seconds);
            this.date = cal.getTime();
        } catch (Throwable t) {
            throw new IllegalArgumentException("Invalid time string: " + time);
        }

    }

    public void addStuff(String newStuff) {
        stuff.add(newStuff);
    }

    public String getStuff(int index) {
        return stuff.get(index);
    }

    public String getAlarmsString() {
        return stuff.get(stuff.size() - 1);
    }

    public boolean isIntervalRecord() {
        return getAlarmsString().startsWith(RECORD_TYPE_INTERVAL);
    }

    public boolean getAlarm(int alarmIndex) {
        if (alarms == null) {
            String hex = getAlarmsString().substring(1, getAlarmsString().length());
            boolean[] retVal = new boolean[hex.length() * 4];
            int flagCount = 0;
            for (int count = 0; count < hex.length(); count++) {
                int i = Integer.parseInt(hex.substring(count, count + 1), 16);
                String bin = Integer.toBinaryString(i);
                String something = String.format("%04d", Integer.parseInt(bin));
                char[] binaryChars = something.toCharArray();
                for (char c : binaryChars) {
                    if (c == '1') {
                        retVal[flagCount] = true;
                    }
                    flagCount++;
                }
            }
            alarms = retVal;
        }
        return alarms[alarmIndex];
    }
}
