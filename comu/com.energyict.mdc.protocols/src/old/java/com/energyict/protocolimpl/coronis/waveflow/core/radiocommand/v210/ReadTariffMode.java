package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 9:46:24
 */
public class ReadTariffMode extends AbstractRadioCommand {

    public ReadTariffMode(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int tariffMode = 0;
    private int duration = 0;      //1 - 24 hours (daily tariff), 1 - 255 days (seasonal tariff), 1 - 28 days (day mode), 1 - 12 months (month mode)
    private int measuredVolumeInTariffPeriod = 0;
    private int cumulativeMeasuredVolumeInBlock1 = 0;
    private int cumulativeMeasuredVolumeInBlock2 = 0;
    private int cumulativeMeasuredVolumeInBlock3 = 0;


    //For time of use tariffs
    private int startHourOrMonth = 0;
    private int startMinuteOrDay = 0;

    //For rising block tariffs (daily or monthly)
    private int startTime;         //0 - 23 hour, 1 - 28 days
    private int RB1;               //This is the volume threshold for block 1
    private int RB2;               //This is the volume threshold for block 2

    public int getDuration() {
        return duration;
    }

    public int getCumulativeMeasuredVolumeInBlock1() {
        return cumulativeMeasuredVolumeInBlock1;
    }

    public int getCumulativeMeasuredVolumeInBlock2() {
        return cumulativeMeasuredVolumeInBlock2;
    }

    public int getCumulativeMeasuredVolumeInBlock3() {
        return cumulativeMeasuredVolumeInBlock3;
    }

    public int getMeasuredVolumeInTariffPeriod() {
        return measuredVolumeInTariffPeriod;
    }

    public int getRB1() {
        return RB1;
    }

    public int getRB2() {
        return RB2;
    }

    public int getStartHourOrMonth() {
        return startHourOrMonth;
    }

    public int getStartMinuteOrDay() {
        return startMinuteOrDay;
    }

    public int getStartTime() {
        return startTime;
    }

    public int getTariffMode() {
        return tariffMode;
    }

    @Override
    protected void parse(byte[] data) throws IOException {

        int offset = 0;

        tariffMode = data[offset++] & 0xFF;

        if ((tariffMode & 0x80) != 0x80) {
            duration = data[offset++] & 0xFF;
            startHourOrMonth = data[offset++] & 0xFF;
            startMinuteOrDay = data[offset++] & 0xFF;
            measuredVolumeInTariffPeriod = convertBCD(data, offset, 5, true);
        } else {
            duration = data[offset++] & 0xFF;
            startTime = data[offset++] & 0xFF;
            RB1 = convertBCD(data, offset, 2, false);
            offset += 2;
            RB2 = convertBCD(data, offset, 2, false) + RB1;
            offset += 2;
            cumulativeMeasuredVolumeInBlock1 = convertBCD(data, offset, 5, true);
            offset += 5;
            cumulativeMeasuredVolumeInBlock2 = convertBCD(data, offset, 5, true);
            offset += 5;
            cumulativeMeasuredVolumeInBlock3 = convertBCD(data, offset, 5, true);
            offset += 5;
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadTariffMode;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}