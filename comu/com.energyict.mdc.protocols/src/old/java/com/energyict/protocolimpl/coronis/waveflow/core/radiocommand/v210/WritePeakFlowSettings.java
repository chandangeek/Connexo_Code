package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

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
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the peak flow settings, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
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