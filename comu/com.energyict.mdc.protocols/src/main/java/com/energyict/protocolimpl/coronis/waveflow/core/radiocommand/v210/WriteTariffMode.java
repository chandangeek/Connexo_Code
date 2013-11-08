package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 9:46:24
 */
public class WriteTariffMode extends AbstractRadioCommand {

    public WriteTariffMode(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int tariffMode = 0;
    private int duration = 0;      //1 - 24 hours (daily tariff), 1 - 255 days (seasonal tariff), 1 - 28 days (day mode), 1 - 12 months (month mode)

    //For time of use tariffs
    private int startHourOrMonth = 0;
    private int startMinuteOrDay = 0;

    //For rising block tariffs (daily or monthly)
    private int startTime;         //0 - 23 hour, 1 - 28 days
    private int RB1;               //This is the volume threshold for block 1
    private int RB2;               //This is the volume threshold for block 2

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public void setRB1(int RB1) {
        this.RB1 = RB1;
    }

    public void setRB2(int RB2) {
        this.RB2 = RB2;
    }

    public void setStartHourOrMonth(int startHourOrMonth) {
        this.startHourOrMonth = startHourOrMonth;
    }

    public void setStartMinuteOrDay(int startMinuteOrDay) {
        this.startMinuteOrDay = startMinuteOrDay;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    public void setTariffMode(int tariffMode) {
        this.tariffMode = tariffMode;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the tariff mode settings, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        if ((tariffMode & 0x80) != 0x80) {
            return new byte[]{(byte) tariffMode, (byte) duration, (byte) startHourOrMonth, (byte) startMinuteOrDay};
        } else {
            byte[] bytes = new byte[]{(byte) tariffMode, (byte) duration, (byte) startTime};
            return ProtocolTools.concatByteArrays(bytes, getByteFromBCD(RB1, 4), getByteFromBCD(RB2 - RB1, 4));
        }
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteTariffMode;
    }

    public void setTimeOfUseTariffs() {
        tariffMode = tariffMode & 0x7F;        //Set b7 to 0
    }

    public void setRisingBlockTariffs() {
        tariffMode = tariffMode | 0x80;        //Set b7 to 1
    }

    public void setRisingBlockTariffPeriodMode(int periodMode) {
        tariffMode = tariffMode & 0xFC;                     //Reset b1 and b0
        tariffMode = tariffMode | (periodMode + 1);         //Set b1 and b0
    }

    public void setTariffPeriodMode(int periodMode) {
        tariffMode = tariffMode | periodMode;       //Set b0
        tariffMode = tariffMode & 0xFD;             //Reset b1
    }

    public void setLogBlocks(int numberOfLogBlocks) {
        tariffMode = tariffMode & 0x9F;
        tariffMode = tariffMode | ((numberOfLogBlocks - 1) << 5);
    }

    public void setUnit(int scale) {
        if (scale == -3) {
            tariffMode = tariffMode & 0xFB;
        }
        if (scale == 0) {
            tariffMode = tariffMode | 0x04;
        }
    }
}