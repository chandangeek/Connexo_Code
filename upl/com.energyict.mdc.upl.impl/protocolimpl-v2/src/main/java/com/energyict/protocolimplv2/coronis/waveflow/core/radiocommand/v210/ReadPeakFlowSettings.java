package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:18:40
 */
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
    protected void parse(byte[] data) {
        monitoringTimePeriod = (data[0] & 0xFF) >> 3;
        startTime = (data[0] & 0xFF) & 0x07;
        weekOfYear = data[1] & 0xFF;
    }

    @Override
    protected byte[] prepare() {
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
