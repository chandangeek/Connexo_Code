package com.energyict.protocolimpl.iec1107.cewe.ceweprometer.valuefactory;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.CewePrometer;
import com.energyict.protocolimpl.iec1107.cewe.ceweprometer.register.ProRegister;

/**
 * Copyrights
 * Date: 23/05/11
 * Time: 9:35
 */
public class AbstractSingleValueFactory extends AbstractValueFactory {

    private final ProRegister proRegister;
    private final Unit unit;
    private final String description;

    public AbstractSingleValueFactory(String obisCode, CewePrometer proMeter, ProRegister register) {
        this(obisCode, proMeter, register, null);
    }

    public AbstractSingleValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit) {
        this(obisCode, proMeter, register, unit, null);
    }

    public AbstractSingleValueFactory(String obisCode, CewePrometer proMeter, ProRegister register, Unit unit, String description) {
        super(ObisCode.fromString(obisCode), proMeter);
        this.proRegister = register;
        this.unit = unit;
        this.description = description;
    }

    @Override
    public Unit getUnit() {
        return unit == null ? Unit.getUndefined() : unit;
    }

    @Override
    public String getDescription() {
        return description == null ? getObisCode().toString() : description;
    }

    public ProRegister getProRegister() {
        return proRegister;
    }
}
