/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;

import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;
import java.util.Date;

public class EnergyValueFactory extends AbstractValueFactory {

    private ProRegister[] registerArray;
    private int fieldIdx;

    public EnergyValueFactory(ObisCode obisCode, ProRegister[] registerArray, int fieldIdx, CewePrometer proMeter) {
        super(obisCode, proMeter);
        this.registerArray = registerArray;
        this.fieldIdx = fieldIdx;
    }

    public Quantity getQuantity() throws IOException {
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        if (row == -1) {
            String msg = "No historical data for billing point: " + getBillingPointFromObisCode();
            throw new NoSuchRegisterException(msg);
        }
        return new Quantity(registerArray[row].asDouble(fieldIdx), getUnit());
    }

    public Date getToTime() throws IOException {
        if (getBillingPointFromObisCode() == 255) {
            return null;
        }
        int row = getProMeter().getRow(getBillingPointFromObisCode());
        return getProMeter().getRegisters().getrTimestamp()[row].asDate();
    }

    public String getDescription() {
        return getCDescription() + ", " + getFDescription();
    }

}
