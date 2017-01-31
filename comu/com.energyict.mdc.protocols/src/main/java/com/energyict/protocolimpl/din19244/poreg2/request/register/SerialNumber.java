/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.din19244.poreg2.request.register;

import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterDataParser;
import com.energyict.protocolimpl.din19244.poreg2.core.RegisterGroupID;

public class SerialNumber extends AbstractRegister {

    public SerialNumber(Poreg poreg, int registerAddress, int fieldAddress, int numberOfRegisters, int numberOfFields) {
        super(poreg, registerAddress, fieldAddress, numberOfRegisters, numberOfFields);
    }

    public SerialNumber(Poreg poreg) {
        super(poreg, 0, 0, 1, 1);
    }

    private String serialNumber;

    public String getSerialNumber() {
        return serialNumber;
    }

    @Override
    protected int getRegisterGroupID() {
        return RegisterGroupID.SerialNumber.getId();
    }

    @Override
    protected void parse(byte[] data) {
        serialNumber = RegisterDataParser.parseAsciiBytes(data);
    }
}