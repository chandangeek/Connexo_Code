/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.util.Date;

public abstract class G3Mapping {

    private final ObisCode obis;

    protected G3Mapping(ObisCode obis) {
        this.obis = obis;
    }

    public RegisterValue readRegister(AS330D as330D) throws IOException {
        return readRegister(as330D.getSession().getCosemObjectFactory());
    }

    public abstract RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException;

    public final ObisCode getObisCode() {
        return obis;
    }

    public ObisCode getBaseObisCode() {
        return getObisCode();
}

    /**
     * meterTimeZone, Unit and CaptureTime are optional (they can be null), in this case they will not be used to construct the RegisterValue.
     * They are only relevant for e.g. extended register.
     */
    public abstract RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException;

    public RegisterValue parse(AbstractDataType abstractDataType) throws IOException {
        return parse(abstractDataType, null, null);
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit) throws IOException {
        return parse(abstractDataType, unit, null);
    }

    public abstract int getDLMSClassId();

    /**
     * Sometimes it's necessary to read out multiple attributes (e.g. value, unit and capture time in case of extended register)
     * The proper subclasses can specify this by overriding this method
     */
    public int[] getAttributeNumbers() {
        return new int[]{getAttributeNumber()};
    }

    /**
     * Usually the value attribute (2) is requested, subclasses can override this
     */
    protected int getAttributeNumber() {
        return 2;
    }
}