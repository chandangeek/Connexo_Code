package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

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
    protected void parse(byte[] data) {
        if ((data[0] & 0xFF) == 0xFF) {
            throw createWaveFlowException("Error writing the customer number, returned 0xFF");
        }
    }

    @Override
    protected byte[] prepare() {
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