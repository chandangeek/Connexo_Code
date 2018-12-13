package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

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
        dtd.setCodeValue(slice.tariffCode());
        dtd.setFrom(slice.start());
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

    private void setCodeValue(String codeValue) {
        this.setCodeValue(Integer.parseInt(codeValue));
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

    private void setFrom(LocalTime from) {
        this.setFrom(from.getHour() * 10_000 + from.getMinute() * 100 + from.getSecond());
    }

    public String getDayTypeName() {
        return dayTypeName;
    }

    public void setDayTypeName(String dayTypeName) {
        this.dayTypeName = dayTypeName;
    }

    @Override
    public String toString() {
        return "CodeDayTypeDefObject" +
                "{codeValue=" + codeValue +
                ", dayTypeId=" + dayTypeId +
                ", dayTypeName='" + dayTypeName + '\'' +
                ", from=" + from +
                '}';
    }

    public int compareTo(CodeDayTypeDefObject other) {
        return this.from - (other == null ? 0 : other.from);
    }
}
