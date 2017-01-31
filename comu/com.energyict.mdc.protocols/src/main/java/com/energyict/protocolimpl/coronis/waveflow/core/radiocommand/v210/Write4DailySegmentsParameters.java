/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

public class Write4DailySegmentsParameters extends AbstractRadioCommand {

    public Write4DailySegmentsParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int segment1StartHour;
    private int segment1StartMinute;
    private int segment1StopHour;
    private int segment1StopMinute;
    private int segment2StopHour;
    private int segment2StopMinute;
    private int segment3StopHour;
    private int segment3StopMinute;
    private int segment4StopHour;
    private int segment4StopMinute;

    private int periodMode;     //0 = day, 1 = week
    private int period;         //1 - 28 (days) or 1 - 52 (weeks)
    private int startYear;
    private int startMonth;
    private int startDay;

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setPeriodMode(int periodMode) {
        this.periodMode = periodMode;
    }

    public void setSegment1StartHour(int segment1StartHour) {
        this.segment1StartHour = segment1StartHour;
    }

    public void setSegment1StartMinute(int segment1StartMinute) {
        this.segment1StartMinute = segment1StartMinute;
    }

    public void setSegment1StopHour(int segment1StopHour) {
        this.segment1StopHour = segment1StopHour;
    }

    public void setSegment1StopMinute(int segment1StopMinute) {
        this.segment1StopMinute = segment1StopMinute;
    }

    public void setSegment2StopHour(int segment2StopHour) {
        this.segment2StopHour = segment2StopHour;
    }

    public void setSegment2StopMinute(int segment2StopMinute) {
        this.segment2StopMinute = segment2StopMinute;
    }

    public void setSegment3StopHour(int segment3StopHour) {
        this.segment3StopHour = segment3StopHour;
    }

    public void setSegment3StopMinute(int segment3StopMinute) {
        this.segment3StopMinute = segment3StopMinute;
    }

    public void setSegment4StopHour(int segment4StopHour) {
        this.segment4StopHour = segment4StopHour;
    }

    public void setSegment4StopMinute(int segment4StopMinute) {
        this.segment4StopMinute = segment4StopMinute;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear - 2000;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the 4 daily segments parameters, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte[] bytes = new byte[14];
        bytes[0] = (byte) segment1StartHour;
        bytes[1] = (byte) segment1StartMinute;
        bytes[2] = (byte) segment1StopHour;
        bytes[3] = (byte) segment1StopMinute;
        bytes[4] = (byte) segment2StopHour;
        bytes[5] = (byte) segment2StopMinute;    
        bytes[6] = (byte) segment3StopHour;
        bytes[7] = (byte) segment3StopMinute;
        bytes[8] = (byte) segment4StopHour;
        bytes[9] = (byte) segment4StopMinute;
        bytes[10] = (byte) ((periodMode << 7) | period);
        bytes[11] = (byte) startYear;
        bytes[12] = (byte) startMonth;
        bytes[13] = (byte) startDay;
        return bytes;
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.Write4DailySegmentsParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}