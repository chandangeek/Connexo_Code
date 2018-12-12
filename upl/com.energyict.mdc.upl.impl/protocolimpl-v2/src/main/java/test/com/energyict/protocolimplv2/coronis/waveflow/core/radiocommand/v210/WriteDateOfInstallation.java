package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:34:02
 */
public class WriteDateOfInstallation extends AbstractRadioCommand {

    public WriteDateOfInstallation(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int day;     //1 - 31
    private int month;   //1 - 12
    private int year;    //Value - 2000

    public void setDay(int day) {
        this.day = day;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public void setYear(int year) {
        this.year = year - 2000;
    }

    @Override
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the date of installation, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() {
        return new byte[]{(byte) day, (byte) month, (byte) year};
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteDateOfInstallation;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}