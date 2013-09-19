package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:13:34
 */
public class WritePeakFlowSettings extends AbstractRadioCommand {

    public WritePeakFlowSettings(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int monitoringTimePeriod = 0;   //1 - 28 days
    private int startTime = 0;              //Day of week: 0 = sunday
    private int weekOfYear = 0;             //1 - 52 week

    public void setMonitoringTimePeriod(int monitoringTimePeriod) {
        this.monitoringTimePeriod = monitoringTimePeriod;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setWeekOfYear(int weekOfYear) {
        this.weekOfYear = weekOfYear;
    }

    @Override
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the peak flow settings, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) ((monitoringTimePeriod << 3) | startTime), (byte) weekOfYear};
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WritePeakFlowSettings;
    }
}