package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.util.Date;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 9:05
 */
public class ExtendedRegisterMapping extends G3Mapping {

    public ExtendedRegisterMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(DlmsSession session) throws IOException {
        final ExtendedRegister extendedRegister = session.getCosemObjectFactory().getExtendedRegister(getObisCode());
        return parse(extendedRegister.getValueAttr(), extendedRegister.getScalerUnit().getEisUnit(), extendedRegister.getCaptureTime());
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        final Quantity quantityValue = new Quantity(abstractDataType.longValue(), unit);
        return new RegisterValue(getObisCode(), quantityValue, captureTime);
    }

    @Override
    public int[] getAttributeNumbers() {
        return new int[]{ExtendedRegisterAttributes.VALUE.getAttributeNumber(), ExtendedRegisterAttributes.UNIT.getAttributeNumber(), ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(),};
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.EXTENDED_REGISTER.getClassId();
    }
}
