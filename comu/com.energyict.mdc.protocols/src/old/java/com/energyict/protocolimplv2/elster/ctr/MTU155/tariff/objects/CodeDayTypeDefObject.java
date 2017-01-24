package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects;

import com.elster.jupiter.calendar.EventOccurrence;

import java.io.Serializable;
import java.time.LocalTime;

/**
 * Copyrights EnergyICT
 * Date: 4/04/11
 * Time: 14:50
 */
public class CodeDayTypeDefObject implements Serializable, Comparable<CodeDayTypeDefObject> {

    private long dayTypeId;
    private String dayTypeName;
    private int from;
    private long codeValue;

    public static CodeDayTypeDefObject from(EventOccurrence eventOccurrence) {
        CodeDayTypeDefObject dtd = new CodeDayTypeDefObject();
        dtd.setCodeValue(eventOccurrence.getEvent().getCode());
        dtd.setFrom(eventOccurrence.getFrom());
        dtd.setDayTypeId(eventOccurrence.getDayType().getId());
        dtd.setDayTypeName(eventOccurrence.getDayType().getName());
        return dtd;
    }

    public long getCodeValue() {
        return codeValue;
    }

    public void setCodeValue(long codeValue) {
        this.codeValue = codeValue;
    }

    public long getDayTypeId() {
        return dayTypeId;
    }

    public void setDayTypeId(long dayTypeId) {
        this.dayTypeId = dayTypeId;
    }

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    private void setFrom(LocalTime from) {
        this.setFrom(from.getHour() * 10000 + from.getMinute() * 100 + from.getSecond());
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