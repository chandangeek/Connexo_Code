package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeCalendarObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeDayTypeObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 13/04/11
 * Time: 11:39
 */
public class RawSpecialDay extends AbstractField<RawSpecialDay> {

    public static final int LENGTH = 2;

    private int month;
    private int day;
    private int dayType;

    private static final Map<String, Integer> DAY_TYPE_CODES = new HashMap<String, Integer>();
    static {
        DAY_TYPE_CODES.put(CodeDayTypeObject.FORCED_1, 1);
        DAY_TYPE_CODES.put(CodeDayTypeObject.FORCED_2, 2);
        DAY_TYPE_CODES.put(CodeDayTypeObject.FORCED_3, 3);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT1_WEEKDAY, 4);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT1_SATURDAY, 5);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT1_HOLIDAY, 6);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT2_WEEKDAY, 4);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT2_SATURDAY, 5);
        DAY_TYPE_CODES.put(CodeDayTypeObject.PT2_HOLIDAY, 6);
        DAY_TYPE_CODES.put(CodeDayTypeObject.DEFAULT, 7);
    }

    public RawSpecialDay(CodeCalendarObject specialDay) {
        month = specialDay.getMonth();
        day = specialDay.getDay();
        dayType = getDayTypeFromDayTypeName(specialDay.getDayTypeName());
    }

    private int getDayTypeFromDayTypeName(String dayTypeName) {
        Integer dayTypeValue = DAY_TYPE_CODES.get(dayTypeName);
        return dayTypeValue != null ? dayTypeValue : 0;
    }

    public RawSpecialDay() {
        month = 0;
        day = 0;
        dayType = 0;
    }

    public byte[] getBytes() {
        byte[] rawData = new byte[LENGTH];
        rawData[1] = 0;
        rawData[1] |= (day != 0 ? day-1 : day) & 0x01F;
        rawData[0] = 0;
        rawData[0] |= (dayType << 5) & 0x0E0;
        rawData[0] |= (month != 0 ? month-1: month) & 0x00F;
        return rawData;
    }

    public RawSpecialDay parse(byte[] rawData, int offset) throws CTRParsingException {
        int ptr = offset;
        dayType = (rawData[ptr] >> 5) & 0x07;
        month = rawData[ptr++] & 0x0F;
        day = rawData[ptr++] & 0x1F;
        return this;
    }

    public int getLength() {
        return getBytes().length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("rawValue = ").append(ProtocolTools.getHexStringFromBytes(getBytes())).append(", ");
        sb.append("day = ").append(day).append(", ");
        sb.append("month = ").append(month).append(", ");
        sb.append("dayType = ").append(dayType);
        return sb.toString();
    }

}
