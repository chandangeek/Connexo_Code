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
public class FloatValueFactory extends AbstractSingleValueFactory {

    public FloatValueFactory(String obisCode, CewePrometer proMeter, ProRegister register) {
        super(obisCode, proMeter, register);
    }

    public FloatValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit) {
        super(obisCode, proMeter, register, unit);
    }

    public FloatValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit, String description) {
        super(obisCode, proMeter, register, unit, description);
    }

    @Override
    public Quantity getQuantity() throws IOException {
        try {
            Double value = getProRegister().asDouble();
            return new Quantity(value, getUnit());
        } catch (IOException e) {
            return null; // Absorb and return null. This will result in 'RegisterNotSupportedException' further on
        }
    }

}
