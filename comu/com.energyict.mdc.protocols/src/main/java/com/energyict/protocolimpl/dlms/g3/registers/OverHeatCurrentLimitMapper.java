/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.RegisterMonitor;

import java.io.IOException;
import java.util.Date;

public class OverHeatCurrentLimitMapper extends G3Mapping {

    private final Unit unit;

    public OverHeatCurrentLimitMapper(ObisCode obisCode, Unit unit) {
        super(obisCode);
        this.unit = unit;
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        RegisterMonitor registerMonitor = cosemObjectFactory.getRegisterMonitor(getObisCode());
        return parse(registerMonitor.readThresholds(), unit);
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        Array thresholds = ((Array) abstractDataType);

        String separator = "";
        StringBuilder sb = new StringBuilder();
        for (int index = 0; index < thresholds.getAllDataTypes().size(); index++) {
            sb.append(separator);
            sb.append("CurrentLimit");
            sb.append(String.valueOf(index + 1));
            sb.append(": ");
            sb.append(((Unsigned8) thresholds.getDataType(index)).getValue());
            sb.append(" ");
            sb.append(this.unit.toString());
            separator = ", ";
        }
        return new RegisterValue(getObisCode(), sb.toString());
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.REGISTER_MONITOR.getClassId();
    }

}
