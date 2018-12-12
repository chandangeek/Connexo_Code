package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import test.com.energyict.protocolimplv2.coronis.common.TimeDateRTCParser;
import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:26:04
 */
public class ReadPeakFlowData extends AbstractRadioCommand {

    public ReadPeakFlowData(WaveFlow waveFlow) {
        super(waveFlow);
    }

    int currentPeriodPeakFlow = -1;
    int lastYearPeakFlow = -1;
    Date currentPeriodPeakFlowTimeStamp;
    Date lastYearPeakFlowTimeStamp;

    public int getCurrentPeriodPeakFlow() {
        return currentPeriodPeakFlow;
    }

    public Date getCurrentPeriodPeakFlowTimeStamp() {
        return currentPeriodPeakFlowTimeStamp;
    }

    public int getLastYearPeakFlow() {
        return lastYearPeakFlow;
    }

    public Date getLastYearPeakFlowTimeStamp() {
        return lastYearPeakFlowTimeStamp;
    }

    @Override
    protected void parse(byte[] data) {
        int offset = 0;
        if (data[offset] == -1) {
            return;
        }
        currentPeriodPeakFlow = convertBCD(data, offset, 4, true);      //Unit: liter per hour
        offset += 4;

        currentPeriodPeakFlowTimeStamp = TimeDateRTCParser.parse(data, offset, 6, getWaveFlow().getTimeZone()).getTime();
        offset += 6;

        if (data[offset] == -1) {
            return;
        }
        lastYearPeakFlow = convertBCD(data, offset, 4, true);           //Unit: liter per hour
        offset += 4;

        lastYearPeakFlowTimeStamp = TimeDateRTCParser.parse(data, offset, 6, getWaveFlow().getTimeZone()).getTime();
        offset += 6;
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadPeakFlowData;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}
