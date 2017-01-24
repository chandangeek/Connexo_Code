package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:43:35
 */
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