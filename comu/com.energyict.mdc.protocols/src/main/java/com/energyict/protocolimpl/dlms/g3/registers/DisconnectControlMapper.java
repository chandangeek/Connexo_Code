package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.Disconnector;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.math.BigDecimal;

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
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final Disconnector disconnector = as330D.getSession().getCosemObjectFactory().getDisconnector(getObisCode());
        final TypeEnum controlState = disconnector.getControlState();
        final BigDecimal value = BigDecimal.valueOf(controlState.getValue());
        final Quantity quantity = new Quantity(value, Unit.get(""));
        return new RegisterValue(getObisCode(), quantity);
    }

}
