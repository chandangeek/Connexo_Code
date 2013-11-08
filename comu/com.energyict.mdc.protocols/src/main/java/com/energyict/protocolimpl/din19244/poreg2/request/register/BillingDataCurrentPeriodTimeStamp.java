package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.*;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Class for reading out the current billing timestamp
 *
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 11:10:31
 */
public class BillingDataCurrentPeriodTimeStamp extends AbstractRegister {

    public BillingDataCurrentPeriodTimeStamp(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.BillingDataCurrentPeriodTimeStamp.getId();
    }

    private Date timeStamp;

    public Date getTimeStamp() {
        return timeStamp;
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getNumberOfFields());
        timeStamp = DinTimeParser.calcDate(poreg, values.get(0).getValue());
    }
}
