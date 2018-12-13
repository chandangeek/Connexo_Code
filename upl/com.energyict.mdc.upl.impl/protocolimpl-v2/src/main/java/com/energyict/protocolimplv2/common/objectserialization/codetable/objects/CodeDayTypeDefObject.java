package com.energyict.protocolimplv2.common.objectserialization.codetable.objects;

import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:50
 */
public class CodeDayTypeDefObject implements Serializable, Comparable<CodeDayTypeDefObject> {

    private int dayTypeId;
    private String dayTypeName;
    private int from;
    private int codeValue;

    public static CodeDayTypeDefObject fromCodeDayTypeDef(TariffCalendarExtractor.CalendarDayTypeSlice slice) {
        CodeDayTypeDefObject dtd = new CodeDayTypeDefObject();
        dtd.setCodeValue(Integer.parseInt(slice.tariffCode()));
        LocalTime start = slice.start();
        dtd.setFrom(start.getHour() * 10000 + start.getMinute() * 100 + start.getSecond());
        dtd.setDayTypeId(Integer.parseInt(slice.dayTypeId()));
        dtd.setDayTypeName(slice.dayTypeName());
        return dtd;
    }

    public int getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(int codeValue) {
        this.codeValue = codeValue;
    }

    public int getDayTypeId() {
        return dayTypeId;
    }

    public void setDayTypeId(int dayTypeId) {
        this.dayTypeId = dayTypeId;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    public String getDayTypeName() {
        return dayTypeName;
    }

    public void setDayTypeName(String dayTypeName) {
        this.dayTypeName = dayTypeName;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("CodeDayTypeDefObject");
        sb.append("{codeValue=").append(codeValue);
        sb.append(", dayTypeId=").append(dayTypeId);
        sb.append(", dayTypeName='").append(dayTypeName).append('\'');
        sb.append(", from=").append(from);
        sb.append('}');
        return sb.toString();
    }

    public int compareTo(CodeDayTypeDefObject other) {
        return this.from - (other == null ? 0 : other.from);
    }
}
