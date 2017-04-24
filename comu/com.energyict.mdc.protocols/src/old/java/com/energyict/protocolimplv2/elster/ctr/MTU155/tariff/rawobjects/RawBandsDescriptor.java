/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.rawobjects;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.common.AbstractField;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRParsingException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.tariff.objects.CodeObject;

import java.util.Arrays;

public class RawBandsDescriptor extends AbstractField<RawBandsDescriptor> {

    private static final int NUMBER_OF_SEASONS = 2;
    private static final int LENGTH = 17 * NUMBER_OF_SEASONS;

    private int[] startMonth;
    private int[] startDay;
    private RawDayTypeBand[] weekday;
    private RawDayTypeBand[] saturday;
    private RawDayTypeBand[] holiday;

    public RawBandsDescriptor() {
        startMonth = new int[NUMBER_OF_SEASONS];
        startDay = new int[NUMBER_OF_SEASONS];
        weekday = new RawDayTypeBand[NUMBER_OF_SEASONS];
        saturday = new RawDayTypeBand[NUMBER_OF_SEASONS];
        holiday = new RawDayTypeBand[NUMBER_OF_SEASONS];

        Arrays.fill(weekday, new RawDayTypeBand());
        Arrays.fill(saturday, new RawDayTypeBand());
        Arrays.fill(holiday, new RawDayTypeBand());

    }

    public RawBandsDescriptor(CodeObject codeObject) {
        this();
        int year = codeObject.getYearFrom();
        for (int i = 0; i < startMonth.length; i++) {
            startMonth[i] = codeObject.getSeasonSet().getSeason(i + 1).getStartMonth(year);
            startDay[i] = codeObject.getSeasonSet().getSeason(i + 1).getStartDay(year);
            weekday[i] = new RawDayTypeBand(codeObject.getWeekday(i + 1));
            saturday[i] = new RawDayTypeBand(codeObject.getSaturday(i + 1));
            holiday[i] = new RawDayTypeBand(codeObject.getHoliday(i + 1));
        }
    }

    public byte[] getBytes() {
        byte[] rawData = new byte[LENGTH];
        int ptr = 0;
        for (int i = 0; i < startMonth.length; i++) {
            rawData[ptr++] = (byte) startMonth[i];
            rawData[ptr++] = (byte) startDay[i];

            byte[] weekdayBytes = weekday[i].getBytes();
            for (byte weekdayByte : weekdayBytes) {
                rawData[ptr++] = weekdayByte;
            }

            byte[] saturdayBytes = saturday[i].getBytes();
            for (byte saturdayByte : saturdayBytes) {
                rawData[ptr++] = saturdayByte;
            }

            byte[] holidayBytes = holiday[i].getBytes();
            for (byte holidayByte : holidayBytes) {
                rawData[ptr++] = holidayByte;
            }
        }
        return rawData;
    }

    public RawBandsDescriptor parse(byte[] rawData, int offset) throws CTRParsingException {
        return this;
    }

    public int getLength() {
        return getBytes().length;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("rawData = ").append(ProtocolTools.getHexStringFromBytes(getBytes())).append('\n');
        for (int i = 0; i < startMonth.length; i++) {
            sb.append("PT").append(i+1).append("_start = ").append(startDay[i]).append('/').append(startMonth[i]).append('\n');
            sb.append("PT").append(i+1).append("_weekday = ").append(weekday[i]).append('\n');
            sb.append("PT").append(i+1).append("_saturday = ").append(saturday[i]).append('\n');
            sb.append("PT").append(i+1).append("_holiday = ").append(holiday[i]).append('\n');
        }
        return sb.toString();
    }
}
