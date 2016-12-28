package com.energyict.protocolimpl.modbus.core;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

public class InputStatusRegister extends AbstractRegister {

    public InputStatusRegister(int reg, int range) {
        super(reg,range,null,"");
    }

    public InputStatusRegister(int reg, int range, String name) {
        super(reg,range,null,name);
    }

    public InputStatusRegister(int reg, int range, ObisCode obisCode) {
       super(reg,range,obisCode,obisCode.getUnitElectricity(0),obisCode.toString());
    }

    public InputStatusRegister(int reg, int range, ObisCode obisCode, String name) {
       super(reg,range,obisCode,Unit.get(""),name);
    }

    public InputStatusRegister(int reg, int range, ObisCode obisCode, Unit unit) {
       super(reg,range,obisCode,unit,obisCode.toString());
    }

    public InputStatusRegister(int reg, int range, ObisCode obisCode, Unit unit, String name) {
        super(reg,range,obisCode,unit,name);
    }
}
