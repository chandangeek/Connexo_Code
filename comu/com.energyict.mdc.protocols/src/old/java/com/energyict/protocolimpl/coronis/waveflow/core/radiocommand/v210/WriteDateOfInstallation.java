/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

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
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the date of installation, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
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