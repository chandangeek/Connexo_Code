/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

public class Read4DailySegmentsParameters extends AbstractRadioCommand {

    public Read4DailySegmentsParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int[] segmentStartHour = new int[4];
    private int[] segmentStartMinute = new int[4];
    private int[] segmentStopHour = new int[4];
    private int[] segmentStopMinute = new int[4];

    private int periodMode;     //0 = day, 1 = week
    private int period;         //1 - 28 (days) or 1 - 52 (weeks)
    private int startYear;
    private int startMonth;
    private int startDay;

    public int getPeriod() {
        return period;
    }

    public int getPeriodMode() {
        return periodMode;
    }

    public int[] getSegmentStartHour() {
        return segmentStartHour;
    }

    public int[] getSegmentStartMinute() {
        return segmentStartMinute;
    }

    public int[] getSegmentStopHour() {
        return segmentStopHour;
    }

    public int[] getSegmentStopMinute() {
        return segmentStopMinute;
    }

    public int getStartDay() {
        return startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        segmentStartHour[0] = data[offset++] & 0xFF;
        segmentStartMinute[0] = data[offset++] & 0xFF;
        segmentStopHour[0] = data[offset++] & 0xFF;
        segmentStopMinute[0] = data[offset++] & 0xFF;
        segmentStartHour[1] = segmentStopHour[0];
        segmentStartMinute[1] = segmentStopMinute[0];
        segmentStopHour[1] = data[offset++] & 0xFF;
        segmentStopMinute[1] = data[offset++] & 0xFF;
        segmentStartHour[2] = segmentStopHour[1];
        segmentStartMinute[2] = segmentStopMinute[1];
        segmentStopHour[2] = data[offset++] & 0xFF;
        segmentStopMinute[2] = data[offset++] & 0xFF;
        segmentStartHour[3] = segmentStopHour[2];
        segmentStartMinute[3] = segmentStopMinute[2];
        segmentStopHour[3] = data[offset++] & 0xFF;
        segmentStopMinute[3] = data[offset++] & 0xFF;

        periodMode = (data[offset] & 0xFF) >> 7;
        period = (data[offset++] & 0xFF) & 0x3F;

        startYear = (data[offset++] & 0xFF) + 2000;
        startMonth = (data[offset++] & 0xFF);
        startDay = (data[offset++] & 0xFF);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.Read4DailySegmentsParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }

    public int getStartHour(int segmentId) {
        return getSegmentStartHour()[segmentId];
    }

    public int getStartMinute(int segmentId) {
        return getSegmentStartMinute()[segmentId];
    }

    public int getStopHour(int segmentId) {
        return getSegmentStopHour()[segmentId];
    }

    public int getStopMinute(int segmentId) {
        return getSegmentStopMinute()[segmentId];
    }

    public String getDescription() {
        return getStartDay() + "/" + getStartMonth() + "/" + getStartYear();
    }

    public Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(getStartYear(), getStartMonth() - 1, getStartDay(), 0, 0, 0);
        return cal.getTime();
    }

    public Quantity getPeriodQuantity() {
        return new Quantity(getPeriod(), getPeriodMode() == 0 ? Unit.get(BaseUnit.DAY) : Unit.get(BaseUnit.WEEK));
    }
}