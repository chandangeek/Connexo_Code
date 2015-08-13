package com.elster.protocolimpl.dlms.tariff.objects;

import com.elster.protocolimpl.dlms.tariff.Holidays;
import com.energyict.mdw.core.CodeCalendar;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:34
 */
@SuppressWarnings("unused")
public class CodeCalendarObject implements Serializable {

    private int codeId;
    private String dayTypeName;
    private int year;
    private int month;
    private int day;
    private int dayOfWeek;
    private int seasonId;

    private static final Calendar[] HOLIDAYS = new Calendar[]{
            ProtocolTools.createCalendar(2000, 1, 1),
            ProtocolTools.createCalendar(2000, 1, 6),
            null, // Easter monday has no fixed date!
            ProtocolTools.createCalendar(2000, 4, 25),
            ProtocolTools.createCalendar(2000, 5, 1),
            ProtocolTools.createCalendar(2000, 6, 2),
            ProtocolTools.createCalendar(2000, 8, 15),
            ProtocolTools.createCalendar(2000, 10, 1),
            ProtocolTools.createCalendar(2000, 12, 8),
            ProtocolTools.createCalendar(2000, 12, 25),
            ProtocolTools.createCalendar(2000, 12, 26)
    };

    public static CodeCalendarObject fromCodeCalendar(CodeCalendar codeCalendar) {
        CodeCalendarObject cc = new CodeCalendarObject();
        cc.setCodeId(codeCalendar.getCode().getId());
        cc.setDayTypeName(codeCalendar.getDayType().getName());
        cc.setYear(codeCalendar.getYear());
        cc.setMonth(codeCalendar.getMonth());
        cc.setDay(codeCalendar.getDay());
        cc.setDayOfWeek(codeCalendar.getDayOfWeek());
        cc.setSeasonId(codeCalendar.getSeason());
        return cc;
    }

    public int getCodeId() {
        return codeId;
    }

    public void setCodeId(int codeId) {
        this.codeId = codeId;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(int dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public String getDayTypeName() {
        return dayTypeName;
    }

    public void setDayTypeName(String dayTypeName) {
        this.dayTypeName = dayTypeName;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getSeasonId() {
        return seasonId;
    }

    public void setSeasonId(int seasonId) {
        this.seasonId = seasonId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean isHoliday() {
        if (isFullDate()) {
            if (dayTypeName.endsWith("Holiday")) {
                for (Calendar holiday : HOLIDAYS) {
                    if (holiday != null) {
                        holiday.set(Calendar.YEAR, getYear());
                        if (isSameDate(holiday)) {
                            return true;
                        }
                    } else {
                        if (isSameDate(Holidays.getEasterMonday(getYear()))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public int getHolidayID() {
        if (isHoliday()) {
            for (int i = 0; i < HOLIDAYS.length; i++) {
                Calendar holiday = HOLIDAYS[i];
                if (holiday != null) {
                    holiday.set(Calendar.YEAR, getYear());
                    if (isSameDate(holiday)) {
                        return i;
                    }
                } else {
                    if (isSameDate(Holidays.getEasterMonday(getYear()))) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public boolean isCustomDay() {
        return isFullDate() && !isHoliday();
    }

    public boolean isSpecialDay() {
        return isCustomDay() || isHoliday();
    }

    private boolean isFullDate() {
        return (year != -1) && (month != -1) && (day != -1);
    }

    public boolean isSameDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return isSameDate(calendar);
    }

    public boolean isSameDate(Calendar calendar) {
        Calendar cc = getCalendar();
        if (calendar != null) {
            boolean same = true;
            same = same && (cc.get(Calendar.YEAR) == calendar.get(Calendar.YEAR));
            same = same && (cc.get(Calendar.MONTH) == calendar.get(Calendar.MONTH));
            same = same && (cc.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH));
            return same;
        } else {
            return false;
        }
    }

    public boolean isSameDate(CodeCalendarObject codeCalendarObject) {
        return isSameDate(codeCalendarObject.getCalendar());
    }

    public Calendar getCalendar() {
        return isFullDate() ? ProtocolTools.createCalendar(year, month, day) : null;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CodeCalendarObject) {
            CodeCalendarObject that = (CodeCalendarObject) obj;
            boolean same = true;
            same = same && (this.year == that.year);
            same = same && (this.month == that.month);
            same = same && (this.day == that.day);
            same = same && that.dayTypeName.startsWith(this.dayTypeName.substring(0, 2));
            same = same && that.dayTypeName.startsWith(this.dayTypeName.substring(3, dayTypeName.length() - 1), 3);
            return same;
        }
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeCalendarObject");
        sb.append("{codeId=").append(codeId);
        sb.append(", dayTypeId=").append(dayTypeName);
        sb.append(", year=").append(year);
        sb.append(", month=").append(month);
        sb.append(", day=").append(day);
        sb.append(", dayOfWeek=").append(dayOfWeek);
        sb.append(", seasonId=").append(seasonId);
        sb.append(", holiday=").append(isHoliday());
        sb.append(", customDay=").append(isCustomDay());
        sb.append(", specialDay=").append(isSpecialDay());
        sb.append('}');
        return sb.toString();
    }
}
