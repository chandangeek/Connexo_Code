package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 8:31
 */
public class RegisterMapping extends G3Mapping {

    public RegisterMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Register register = cosemObjectFactory.getRegister(getObisCode());
        return parse(register.getValueAttr(), register.getScalerUnit().getEisUnit());
    }

    @Override
    public int[] getAttributeNumbers() {
        return new int[]{RegisterAttributes.VALUE.getAttributeNumber(), RegisterAttributes.SCALER_UNIT.getAttributeNumber()};
}

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        return new RegisterValue(getObisCode(), new Quantity((abstractDataType).longValue(), unit));
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.REGISTER.getClassId();
    }
}