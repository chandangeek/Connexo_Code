package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 20/05/11
 * Time: 15:17
 */
public class InstantValuesFactory extends AbstractValueFactory {

    private final int valueIndex;
    private final ProRegister proRegister;

    private static final Unit[] UNITS = new Unit[]{
            Unit.get(""),
            Unit.get("V"),
            Unit.get("V"),
            Unit.get("V"),
            Unit.get("V"),
            Unit.get("V"),
            Unit.get("V"),
            Unit.get("A"),
            Unit.get("A"),
            Unit.get("A"),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get(""),
            Unit.get("W"),
            Unit.get("W"),
            Unit.get("W"),
            Unit.get("var"),
            Unit.get("var"),
            Unit.get("var"),
            Unit.get("VA"),
            Unit.get("VA"),
            Unit.get("VA"),
            Unit.get("THDVIEEE"),
            Unit.get("THDVIEEE"),
            Unit.get("THDVIEEE"),
            Unit.get("THDIIEEE"),
            Unit.get("THDIIEEE"),
            Unit.get("THDIIEEE"),
            Unit.get("W"),
            Unit.get("var"),
            Unit.get("VA"),
            Unit.get(""),
            Unit.get(""),
            Unit.get("Hz"),
            Unit.get(""),
            Unit.get(""),
            Unit.get("V"),
            Unit.get("A")
    };

    public InstantValuesFactory(String obisCode, ProRegister proRegister, CewePrometer prometer, int valueIndex) {
        super(ObisCode.fromString(obisCode), prometer);
        this.valueIndex = valueIndex;
        this.proRegister = proRegister;
    }

    @Override
    public Quantity getQuantity() throws IOException {
        return new Quantity(getProRegister().asDouble(getValueIndex() - 1), getUnit());
    }

    @Override
    public Unit getUnit() {
        return UNITS[getValueIndex()];
    }

    public ProRegister getProRegister() {
        return proRegister;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    @Override
    public String getDescription() {
        return "Instant value";
    }

}
