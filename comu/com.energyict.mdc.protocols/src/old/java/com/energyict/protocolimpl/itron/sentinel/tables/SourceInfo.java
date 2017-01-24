/*
 * SourceUnits.java
 *
 * Created on 16 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.sentinel.tables;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.ansi.c12.tables.UOMEntryBitField;
import com.energyict.protocolimpl.itron.sentinel.Sentinel;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class SourceInfo {

    Sentinel sentinel;

    /** Creates a new instance of SourceUnits */
    public SourceInfo(Sentinel sentinel) {
        this.sentinel=sentinel;
    }

    public ObisCodeDescriptor getObisCodeDescriptor(int dataControlEntryIndex) throws IOException {
        if (sentinel.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[dataControlEntryIndex]) {
            UOMEntryBitField uomEntryBitField = sentinel.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[dataControlEntryIndex];

            // if nfs bit, manufacturer specific units of measurement are used...
//            if (uomEntryBitField.isNfs()) {
//            }

            UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
            return uom2ObisTranslator.getObisCodeDescriptor();
        }
        return null;
    }

    public Unit getUnit(int dataControlEntryIndex) throws IOException {
        if (sentinel.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[dataControlEntryIndex]) {
            UOMEntryBitField uomEntryBitField = sentinel.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[dataControlEntryIndex];
            return uomEntryBitField.getUnit();
        }
        return null; // KV_TO_DO ??
    }

    public BigDecimal basic2engineering(BigDecimal value, int index) throws IOException {
        return basic2engineering(value, index,true);
    }

    public BigDecimal basic2engineering(BigDecimal bd, int sourceIndex, boolean energy) throws IOException {
        UOMEntryBitField uomEntryBitField=null;

        if (sentinel.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[sourceIndex]) {
            uomEntryBitField = sentinel.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[sourceIndex];


            // KV_TO_DO use ConstantsTable to get the multipliers and constants for a sourceinfo?

            return bd;
        }

        throw new IOException("SourceInfo, basic2engineering(), no UOMEntryBitField, cannot calculate engineering value!");

    } // public BigDecimal basic2engineering(BigDecimal value, boolean loadProfile) throws IOException


}
