package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 13:27:38
 */
public class WriteCumulativeFlowVolumeParameters extends AbstractRadioCommand {

    public WriteCumulativeFlowVolumeParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int band1LowThreshold;
    private int band2LowThreshold;
    private int band3LowThreshold;
    private int band4LowThreshold;
    private int band5LowThreshold;
    private int band6LowThreshold;
    private int band7LowThreshold;
    private int band7HighThreshold;

    private int periodMode;     //0 = day, 1 = week
    private int unitFlag;       //0 = 1/1000th, 1 = normal
    private int period;         //1 - 28 (days) or 1 - 52 (weeks)         
    private int startYear;
    private int startMonth;
    private int startDay;

    public void setBand1LowThreshold(int band1LowThreshold) {
        this.band1LowThreshold = band1LowThreshold;
    }

    public void setUnitFlag(int unitFlag) {
        this.unitFlag = unitFlag;
    }

    public void setBand2LowThreshold(int band2LowThreshold) {
        this.band2LowThreshold = band2LowThreshold;
    }

    public void setBand3LowThreshold(int band3LowThreshold) {
        this.band3LowThreshold = band3LowThreshold;
    }

    public void setBand4LowThreshold(int band4LowThreshold) {
        this.band4LowThreshold = band4LowThreshold;
    }

    public void setBand5LowThreshold(int band5LowThreshold) {
        this.band5LowThreshold = band5LowThreshold;
    }

    public void setBand6LowThreshold(int band6LowThreshold) {
        this.band6LowThreshold = band6LowThreshold;
    }

    public void setBand7LowThreshold(int band7LowThreshold) {
        this.band7LowThreshold = band7LowThreshold;
    }

    public void setBand7HighThreshold(int band7HighThreshold) {
        this.band7HighThreshold = band7HighThreshold;
    }

    public void setPeriod(int period) {
        this.period = period;
    }

    public void setPeriodMode(int periodMode) {
        this.periodMode = periodMode;
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
            throw new WaveFlowException("Error writing the cumulative volume parameters, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        byte[] bytes = new byte[0];
        int periodByte = (periodMode << 7) | (unitFlag << 6) | (period);

        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band1LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band2LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band3LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band4LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band5LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band6LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band7LowThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, getByteFromBCD(band7HighThreshold, 4));
        bytes = ProtocolTools.concatByteArrays(bytes, new byte[]{(byte) periodByte, (byte) startYear, (byte) startMonth, (byte) startDay});

        return bytes;
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteCumulativeFlowVolumeParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}