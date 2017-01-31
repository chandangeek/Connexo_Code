/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Clock;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;

import java.io.IOException;
import java.util.Date;

public class ClockMapping extends G3Mapping {

    public ClockMapping(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        Clock clock = cosemObjectFactory.getClock(getObisCode());
        return parse(null, null, clock.getDateTime());   //Already parse it, and pass it along in the captureTime parameter, because we only have access to the device timezone in this method
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        return new RegisterValue(getObisCode(), captureTime);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.CLOCK.getClassId();
    }
}