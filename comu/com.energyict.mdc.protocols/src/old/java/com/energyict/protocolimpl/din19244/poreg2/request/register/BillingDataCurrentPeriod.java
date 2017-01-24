package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.util.ArrayList;
import java.util.List;

/**
 * Class for reading out the current billing data
 *
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 9:23:31
 */
public class BillingDataCurrentPeriod extends AbstractRegister {

    public BillingDataCurrentPeriod(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.BillingDataCurrentPeriod.getId();
    }

    private List<ExtendedValue> values = new ArrayList<ExtendedValue>();

    public List<ExtendedValue> getValues() {
        return values;
    }

    @Override
    protected void parse(byte[] data) {
        values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getNumberOfFields());
    }
}
