/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

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
    protected void parse(byte[] data) throws IOException {
        day = data[0] & 0xFF;
        month = data[1] & 0xFF;
        year = (data[2] & 0xFF) + 2000;
    }

    @Override
    protected byte[] prepare() throws IOException {
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