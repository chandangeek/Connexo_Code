package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.DinTimeParser;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Class to read out the timestamp for the billing data
 *
 * Copyrights EnergyICT
 * Date: 9-mei-2011
 * Time: 11:10:31
 */
public class BillingDataLastPeriodTimeStamp extends AbstractRegister {

    public BillingDataLastPeriodTimeStamp(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.BillingDataLastPeriodTimeStamp.getId();
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
