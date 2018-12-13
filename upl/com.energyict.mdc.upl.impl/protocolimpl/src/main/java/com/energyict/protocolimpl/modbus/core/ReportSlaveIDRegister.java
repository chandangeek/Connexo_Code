package com.energyict.protocolimpl.modbus.core;

import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;

/**
 * An implementation of [{@link AbstractRegister} made for the 'Slave ID'
 *
 * @author sva
 * @since 20/11/2013 11:33
 */
public class ReportSlaveIDRegister extends AbstractRegister {

    public ReportSlaveIDRegister() {
        this(-1, -1);
    }

    public ReportSlaveIDRegister(int reg, int range) {
        super(reg, range, null, "");
    }

    public ReportSlaveIDRegister(int reg, int range, String name) {
        super(reg, range, null, name);
    }

    public ReportSlaveIDRegister(int reg, int range, ObisCode obisCode) {
        super(reg, range, obisCode, obisCode.getUnitElectricity(0), obisCode.toString());
    }

    public ReportSlaveIDRegister(int reg, int range, ObisCode obisCode, String name) {
        super(reg, range, obisCode, Unit.get(""), name);
    }

    public ReportSlaveIDRegister(int reg, int range, ObisCode obisCode, Unit unit) {
        super(reg, range, obisCode, unit, obisCode.toString());
    }

    public ReportSlaveIDRegister(int reg, int range, ObisCode obisCode, Unit unit, String name) {
        super(reg, range, obisCode, unit, name);
    }

}