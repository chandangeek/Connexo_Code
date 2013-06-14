package com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.util.Calendar;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:34:02
 */
public class ReadDateOfInstallation extends AbstractRadioCommand {

    public ReadDateOfInstallation(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private int day;     //1 - 31
    private int month;   //1 - 12
    private int year;    

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public String getDescription() {
        return getDay() + "/" + getMonth() + "/" + getYear();
    }

    public Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.set(getYear(), getMonth() - 1, getDay(), 0, 0, 0);
        return cal.getTime();
    }

    @Override
    protected void parse(byte[] data) {
        day = data[0] & 0xFF;
        month = data[1] & 0xFF;
        year = (data[2] & 0xFF) + 2000;
    }

    @Override
    protected byte[] prepare() {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadDateOfInstallation;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}