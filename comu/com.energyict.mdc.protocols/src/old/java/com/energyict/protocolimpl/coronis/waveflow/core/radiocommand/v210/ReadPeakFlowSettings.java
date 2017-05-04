/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

public class ReadPeakFlowSettings extends AbstractRadioCommand {

    public ReadPeakFlowSettings(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int monitoringTimePeriod = 0;   //1 - 28 days
    private int startTime = 0;              //Day of week: 0 = sunday
    private int weekOfYear = 0;             //1 - 52 week

    public int getMonitoringTimePeriod() {
        return monitoringTimePeriod;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getWeekOfYear() {
        return weekOfYear;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        monitoringTimePeriod = (data[0] & 0xFF) >> 3;
        startTime = (data[0] & 0xFF) & 0x07;
        weekOfYear = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadPeakFlowSettings;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}
