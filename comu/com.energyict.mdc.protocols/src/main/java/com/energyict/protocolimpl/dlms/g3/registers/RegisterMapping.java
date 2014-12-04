package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.cosem.Register;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;

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
    public RegisterValue readRegister(DlmsSession dlmsSession) throws IOException {
        final Register register = dlmsSession.getCosemObjectFactory().getRegister(getObisCode());
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