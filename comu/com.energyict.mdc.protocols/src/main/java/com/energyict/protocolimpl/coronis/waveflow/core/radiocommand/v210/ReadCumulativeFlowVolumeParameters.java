package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 14:05:27
 */
public class ReadCumulativeFlowVolumeParameters extends AbstractRadioCommand {

    public ReadCumulativeFlowVolumeParameters(WaveFlow waveFlow) {
        super(waveFlow);
    }


    private int[] bandLowThreshold = new int[7];
    private int[] bandHighThreshold = new int[7];

    private int periodMode;     //0 = day, 1 = week
    private int unitFlag;       //0 = 1/1000th, 1 = normal
    private int period;         //1 - 28 (days) or 1 - 52 (weeks)
    private int startYear;
    private int startMonth;
    private int startDay;

    public int getBandHighThreshold(int band) {
        return bandHighThreshold[band];
    }

    public int getBandLowThreshold(int band) {
        return bandLowThreshold[band];
    }

    public String getDescription() {
        return getStartDay() + "/" + getStartMonth() + "/" + getStartYear();
    }

    public Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(getStartYear(), getStartMonth() - 1, getStartDay(), 0, 0, 0);
        return cal.getTime();
    }

    public int getPeriod() {
        return period;
    }

    public int getPeriodMode() {
        return periodMode;
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

    public int getUnitFlag() {
        return unitFlag;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        int offset = 0;

        bandLowThreshold[0] = convertBCD(data, offset, 2, false);
        offset += 2;

        for (int band = 0; band < 6; offset += 2) {
            bandHighThreshold[band] = convertBCD(data, offset, 2, false);
            band++;
            bandLowThreshold[band] = bandHighThreshold[band - 1]; 
        }

        bandHighThreshold[6] = convertBCD(data, offset, 2, false);
        offset += 2;

        periodMode = (data[offset] & 0xFF) >> 7;
        unitFlag = ((data[offset] & 0xFF) >> 6) & 0x01;
        period = (data[offset] & 0xFF) & 0x3F;
        offset++;

        startYear = (data[offset] & 0xFF) + 2000;
        offset++;
        startMonth = (data[offset] & 0xFF);
        offset++;
        startDay = (data[offset] & 0xFF);
        offset++;
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.Read7BandCumulativeVolumeParameters;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}