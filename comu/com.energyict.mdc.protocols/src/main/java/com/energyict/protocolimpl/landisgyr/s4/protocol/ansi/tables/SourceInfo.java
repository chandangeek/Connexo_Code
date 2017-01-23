/*
 * SourceUnits.java
 *
 * Created on 16 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.S4;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class SourceInfo {

    S4 s4;

    /** Creates a new instance of SourceUnits */
    public SourceInfo(S4 s4) {
        this.s4=s4;
    }

    public ObisCodeDescriptor getObisCodeDescriptor(int dataControlEntryIndex) throws IOException {
        UnitOfMeasure uom = UnitOfMeasureFactory.findUnitOfMeasure(dataControlEntryIndex);
        return new ObisCodeDescriptor(1, uom.getObisCField(), uom.getDescription());
    }

    public Unit getUnit(int dataControlEntryIndex) throws IOException {
        UnitOfMeasure uom = UnitOfMeasureFactory.findUnitOfMeasure(dataControlEntryIndex);
        return uom.getUnit();
    }


    public BigDecimal basic2engineering(BigDecimal bd, int dataControlEntryIndex, boolean energy) throws IOException {
        UnitOfMeasure uom = UnitOfMeasureFactory.findUnitOfMeasure(dataControlEntryIndex);
        MeterFactors mf = s4.getManufacturerTableFactory().getMeterFactors();
        if (uom.isCURRENTMultiplier()) {
            return bd.multiply(s4.getManufacturerTableFactory().getServiceTypeTable().getCurrentMultiplier());
        }
        else if (uom.isVOLTMultiplier()) {
            return bd.multiply(s4.getManufacturerTableFactory().getMeterStatus().getVoltageMultiplier());
        }
        else if (uom.isPOWERMultiplier()) {
            if (energy)
                return bd.multiply(mf.getEnergyMultiplier());
            else
                return bd.multiply(mf.getDemandMultiplier());
        }

        return bd;
    } // public BigDecimal basic2engineering(BigDecimal bd, int dataControlEntryIndex, boolean energy) throws IOException

}
