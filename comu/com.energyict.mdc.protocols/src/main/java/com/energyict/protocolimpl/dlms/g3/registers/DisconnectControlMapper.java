package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.dlms.cosem.attributes.DisconnectControlAttribute;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 10:47
 */
public class DisconnectControlMapper extends G3Mapping {

    public DisconnectControlMapper(ObisCode obisCode) {
        super(obisCode);
    }

    @Override
    public RegisterValue readRegister(CosemObjectFactory cosemObjectFactory) throws IOException {
        final Disconnector disconnector = cosemObjectFactory.getDisconnector(getObisCode());
        return parse(disconnector.getControlState());
    }

    @Override
    public int getAttributeNumber() {
        return DisconnectControlAttribute.CONTROL_STATE.getAttributeNumber();
    }

    public RegisterValue parse(AbstractDataType abstractDataType, Unit unit, Date captureTime) throws IOException {
        final TypeEnum controlState = ((TypeEnum) abstractDataType);
        final BigDecimal value = BigDecimal.valueOf(controlState.getValue());
        final Quantity quantity = new Quantity(value, Unit.get(""));
        return new RegisterValue(getObisCode(), quantity);
    }

    @Override
    public int getDLMSClassId() {
        return DLMSClassId.DISCONNECT_CONTROL.getClassId();
    }
}
