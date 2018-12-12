package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;

import java.io.IOException;
import java.util.Date;


/**
 * Copyrights EnergyICT
 * Date: 5/10/16
 * Time: 8:31
 */
public class MetrologyMapping extends G3Mapping {

    public MetrologyMapping(ObisCode obis) {
        super(obis);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Register register = cosemObjectFactory.getRegister(getObisCode());
        AbstractDataType valueAttr = register.getValueAttr();
        Unit unit = register.getScalerUnit().getEisUnit();
        return parse(valueAttr, unit);
    }

    @Override
    public int[] getAttributeNumbers() {
        return new int[]{RegisterAttributes.VALUE.getAttributeNumber(), RegisterAttributes.SCALER_UNIT.getAttributeNumber()};
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        double value = abstractDataType.getFloat64().getValue();

        return new RegisterValue(getObisCode(), new Quantity(value, unit));
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.REGISTER.getClassId();
    }


    @Override
    public int getValueAttribute(){
        return RegisterAttributes.VALUE.getAttributeNumber();
    }

    @Override
    public int getUnitAttribute() {
        return RegisterAttributes.SCALER_UNIT.getAttributeNumber();
    }
}
