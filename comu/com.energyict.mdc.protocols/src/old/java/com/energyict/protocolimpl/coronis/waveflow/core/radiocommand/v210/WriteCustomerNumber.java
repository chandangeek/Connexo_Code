package com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.v210;

import com.energyict.protocolimpl.coronis.core.WaveFlowException;
import com.energyict.protocolimpl.coronis.waveflow.core.WaveFlow;
import com.energyict.protocolimpl.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 16-mei-2011
 * Time: 11:43:35
 */
public class WriteCustomerNumber extends AbstractRadioCommand {

    public WriteCustomerNumber(WaveFlow waveFlow) {
        super(waveFlow);
    }

    private String customerNumber;

    public void setCustomerNumber(String customerNumber) {
        this.customerNumber = customerNumber;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        if ((data[0] & 0xFF) == 0xFF) {
            throw new WaveFlowException("Error writing the customer number, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() throws IOException {
        return customerNumber.getBytes();
    }

    @Override
    protected RadioCommandId getSubCommandId() {
        return RadioCommandId.WriteCustomerNumber;
    }

    @Override
    protected RadioCommandId getRadioCommandId() {
        return RadioCommandId.ExtendedApplicativeCommand;
    }
}