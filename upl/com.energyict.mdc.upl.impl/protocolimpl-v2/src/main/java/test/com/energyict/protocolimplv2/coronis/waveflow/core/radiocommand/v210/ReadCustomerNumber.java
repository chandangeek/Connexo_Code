package test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.v210;

import test.com.energyict.protocolimplv2.coronis.waveflow.WaveFlow;
import test.com.energyict.protocolimplv2.coronis.waveflow.core.radiocommand.AbstractRadioCommand;

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
    protected void parse(byte[] data) {
        customerNumber = new String(data);
    }

    @Override
    protected byte[] prepare() {
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