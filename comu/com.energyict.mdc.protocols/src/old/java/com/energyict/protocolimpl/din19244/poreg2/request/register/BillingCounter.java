/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.io.IOException;
import java.util.List;

public class BillingCounter extends AbstractRegister {

    public BillingCounter(Poreg poreg) {
        super(poreg, 0, 0, 2, 4);
    }

    private int counter = 0;

    public int getCount() {
        return counter;
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.BillingCounter.getId();
    }

    @Override
    protected void parse(byte[] data) throws IOException {
        List<ExtendedValue> values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getTotalReceivedNumberOfRegisters());
        counter = values.get(3).getValue();
    }
}