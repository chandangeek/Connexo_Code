/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

public class ReadCustomerNumber extends AbstractRadioCommand {

    public ReadCustomerNumber(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private String customerNumber;

    public String getCustomerNumber() {
        return customerNumber;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        customerNumber = new String(data);
    }

    @Override
    protected byte[] prepare() throws IOException {
        return new byte[0];
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.ReadCustomerNumber;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}