package com.energyict.protocolimpl.dlms.g3.registers;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.Data;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.g3.AS330D;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Copyrights EnergyICT
 * Date: 22/03/12
 * Time: 10:01
 */
public class DataValueMapping extends G3Mapping {

    private final Unit unit;

    public DataValueMapping(ObisCode obisCode) {
        this(obisCode, Unit.get(""));
    }

    public DataValueMapping(ObisCode obisCode, Unit unit) {
        super(obisCode);
        this.unit = unit;
    }

    @Override
    public RegisterValue readRegister(AS330D as330D) throws IOException {
        final Data data = as330D.getSession().getCosemObjectFactory().getData(getObisCode());
        final AbstractDataType valueAttr = data.getValueAttr();
        final BigDecimal value = BigDecimal.valueOf(valueAttr.longValue());
        final Quantity quantityValue = new Quantity(value, unit);
        return new RegisterValue(getObisCode(), quantityValue);
    }
}
