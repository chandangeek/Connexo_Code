/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.ExtendedValue;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

import java.util.ArrayList;
import java.util.List;

public class Level0Results extends AbstractRegister {

    public Level0Results(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public Level0Results(Poreg poreg) {
        super(poreg, 0, 9, poreg.isPoreg2() ? 32 : 128, 1);           //Only read out field 9: current cumulative active energy
    }

    private List<ExtendedValue> values = new ArrayList<ExtendedValue>();

    public List<ExtendedValue> getValues() {
        return values;
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.Level0Result.getId();
    }

    @Override
    protected void parse(byte[] data) {
        values = RegisterDataParser.parseData(data, getTotalReceivedNumberOfRegisters(), getReceivedNumberOfFields());
    }
}