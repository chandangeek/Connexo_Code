package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 20/05/11
 * Time: 14:41
 */
public class IntegerValueFactory extends AbstractSingleValueFactory {

    public IntegerValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit, String description) {
        super(obisCode, proMeter, register, unit, description);
    }

    public IntegerValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit) {
        super(obisCode, proMeter, register, unit);
    }

    public IntegerValueFactory(String obisCode, CewePrometer proMeter, ProRegister register) {
        super(obisCode, proMeter, register);
    }

    @Override
    public Quantity getQuantity() throws IOException {
        try {
            Integer value = getProRegister().asInteger();
            return new Quantity(value, getUnit());
        } catch (IOException e) {
            return null; // Absorb and return null. This will result in 'RegisterNotSupportedException' further on
        }
    }

}
