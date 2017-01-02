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

    private int from;
    private int codeValue;

    public static CodeDayTypeDefObject fromCodeDayTypeDef(TariffCalendarExtractor.CalendarDayTypeSlice slice) {
        CodeDayTypeDefObject dtd = new CodeDayTypeDefObject();
        dtd.setCodeValue(slice.tariffCode());
        dtd.setFrom(slice.start());
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

    public int getFrom() {
        return from;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    private void setFrom(LocalTime from) {
        this.setFrom(from.getHour() * 10_000 + from.getMinute() * 100 + from.getSecond());
    }

    @Override
    public String toString() {
        return "CodeDayTypeDefObject" +
                "{codeValue=" + codeValue +
                ", from=" + from +
                '}';
    }

    public int compareTo(CodeDayTypeDefObject other) {
        return this.from - (other == null ? 0 : other.from);
    }
}
