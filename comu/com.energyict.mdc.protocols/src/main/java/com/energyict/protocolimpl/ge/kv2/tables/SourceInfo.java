/*
 * SourceUnits.java
 *
 * Created on 16 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2.tables;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.ansi.c12.tables.UOMEntryBitField;
import com.energyict.protocolimpl.ge.kv2.GEKV2;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class SourceInfo {

    GEKV2 gekv2;

    /** Creates a new instance of SourceUnits */
    public SourceInfo(GEKV2 gekv2) {
        this.gekv2=gekv2;
    }

    public ObisCodeDescriptor getObisCodeDescriptor(int dataControlEntryIndex) throws IOException {
        if (gekv2.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[dataControlEntryIndex]) {
            UOMEntryBitField uomEntryBitField = gekv2.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[dataControlEntryIndex];

            int byte0=-1;
            int byte1=-1;
            if (uomEntryBitField.isNfs() && (gekv2.getStandardTableFactory().getSourceDefinitionTable().getDataControlFlag()[dataControlEntryIndex])) {
                byte0 = gekv2.getStandardTableFactory().getDataControlTable().getSourceId()[dataControlEntryIndex][0];
                byte1 = gekv2.getStandardTableFactory().getDataControlTable().getSourceId()[dataControlEntryIndex][1];
            }
            UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField,byte0,byte1);
            return uom2ObisTranslator.getObisCodeDescriptor();
        }
        return null;
    }

    public Unit getUnit(int dataControlEntryIndex) throws IOException {
        if (gekv2.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[dataControlEntryIndex]) {
            UOMEntryBitField uomEntryBitField = gekv2.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[dataControlEntryIndex];
            UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
            return uom2ObisTranslator.getUnit();
        }
        return null; // KV_TO_DO ??
    }

    public Unit getChannelUnit(int sourceIndex) throws IOException {

        if (gekv2.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[sourceIndex]) {
            UOMEntryBitField uomEntryBitField = gekv2.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[sourceIndex];
            UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
            return uom2ObisTranslator.getUnit();
        }
        return null; // KV_TO_DO ??
    }

    public BigDecimal basic2engineering(BigDecimal value, int index) throws IOException {
        return basic2engineering(value, index,true);
    }
    public BigDecimal basic2engineering(BigDecimal value, int index, boolean loadProfile) throws IOException {
        return basic2engineering(value, index,loadProfile,loadProfile);
    }



    public BigDecimal basic2engineering(BigDecimal bd, int index, boolean loadProfile, boolean energy) throws IOException {
        UOMEntryBitField uomEntryBitField=null;
        int sourceIndex=-1;

        if (loadProfile) {
            // source index is load profile channel
            sourceIndex = gekv2.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[index].getLoadProfileSourceSelect();
        }
        else sourceIndex=index;

        if (gekv2.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[sourceIndex]) {
            uomEntryBitField = gekv2.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[sourceIndex];

            long energyScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA();
            long demandScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getDemandScaleFactorVA();
            long voltageL2LScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getVoltSquareLine2LineScaleFactor();
            long voltageL2NScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getVoltSquareLine2NeutralScaleFactor();
            long currentScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getCurrentSquareScaleFactor();
            long currentNeutralScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getNeutralCurrentSquareScaleFactor();

            // parse idCode, netflow & quadrants
            switch(uomEntryBitField.getIdCode()) {
                case 3: // VA phasor
                case 2: // VA apparent
                case 1: // var reactive
                case 0: { // W active
                    bd = bd.multiply(BigDecimal.valueOf(energy?energyScaleFactor:demandScaleFactor));
                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    bd = bd.movePointLeft(6+3); // see kv2 doc
                    return bd;
                }

                case 8: { // RMS volts
                    bd = bd.movePointLeft(1); // see kv2 doc
                    return bd;
                }

                case 10: { // RMS volts squared V²
                    if (uomEntryBitField.getSegmentation() == 4)
                        bd = bd.multiply(BigDecimal.valueOf(voltageL2NScaleFactor));
                    else
                        bd = bd.multiply(BigDecimal.valueOf(voltageL2LScaleFactor));

                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    bd = bd.movePointLeft(6+3); // see kv2 doc
                    return bd;
                }

                case 12: { // RMS amps
                    bd = bd.movePointLeft(1); // see kv2 doc
                    return bd;
                }

                case 14: { // RMS amps squared A²
                    if (uomEntryBitField.getSegmentation() == 4)
                        bd = bd.multiply(BigDecimal.valueOf(currentNeutralScaleFactor));
                    else
                        bd = bd.multiply(BigDecimal.valueOf(currentScaleFactor));

                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    if (loadProfile)
                        bd = bd.movePointLeft(9); // see kv2 doc
                    else
                        bd = bd.movePointLeft(11); // see kv2 doc
                    return bd;
                }

                case 16: // THDV IEEE
                case 17: { // THDI IEEE
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 24:// power factor computed using VA apparent id code 2
                case 25: { // power factor computed using VA phasor id code 3
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 33: { // Frequency
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 34: { // Counter
                    return bd;
                }

                default:
                    throw new IOException("SourceInfo, basic2engineering(), invalid id code "+uomEntryBitField.getIdCode());
            }
        }

        throw new IOException("SourceInfo, basic2engineering(), no UOMEntryBitField, cannot calculate engineering value!");

    } // public BigDecimal basic2engineering(BigDecimal value, boolean loadProfile) throws IOException

    public BigDecimal getMultiplier(int index) throws IOException {
        return getMultiplier(index, true,true);
    }

    public BigDecimal getMultiplier(int index, boolean loadProfile, boolean energy) throws IOException {
        UOMEntryBitField uomEntryBitField=null;
        int sourceIndex=-1;
        BigDecimal bd = BigDecimal.ONE;
        if (loadProfile) {
            // source index is load profile channel
            sourceIndex = gekv2.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[index].getLoadProfileSourceSelect();
        }
        else sourceIndex=index;

        if (gekv2.getStandardTableFactory().getSourceDefinitionTable().getUomEntryFlag()[sourceIndex]) {
            uomEntryBitField = gekv2.getStandardTableFactory().getUnitOfMeasureEntryTable().getUomEntryBitField()[sourceIndex];

            long energyScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getEnergyScaleFactorVA();
            long demandScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getDemandScaleFactorVA();
            long voltageL2LScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getVoltSquareLine2LineScaleFactor();
            long voltageL2NScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getVoltSquareLine2NeutralScaleFactor();
            long currentScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getCurrentSquareScaleFactor();
            long currentNeutralScaleFactor = gekv2.getManufacturerTableFactory().getScaleFactorTable().getNeutralCurrentSquareScaleFactor();

            // parse idCode, netflow & quadrants
            switch(uomEntryBitField.getIdCode()) {
                case 3: // VA phasor
                case 2: // VA apparent
                case 1: // var reactive
                case 0: { // W active
                    bd = bd.multiply(BigDecimal.valueOf(energy?energyScaleFactor:demandScaleFactor));
                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    bd = bd.movePointLeft(6+3); // see kv2 doc
                    return bd;
                }

                case 8: { // RMS volts
                    bd = bd.movePointLeft(1); // see kv2 doc
                    return bd;
                }

                case 10: { // RMS volts squared V²
                    if (uomEntryBitField.getSegmentation() == 4)
                        bd = bd.multiply(BigDecimal.valueOf(voltageL2NScaleFactor));
                    else
                        bd = bd.multiply(BigDecimal.valueOf(voltageL2LScaleFactor));

                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    bd = bd.movePointLeft(6+3); // see kv2 doc
                    return bd;
                }

                case 12: { // RMS amps
                    bd = bd.movePointLeft(1); // see kv2 doc
                    return bd;
                }

                case 14: { // RMS amps squared A²
                    if (uomEntryBitField.getSegmentation() == 4)
                        bd = bd.multiply(BigDecimal.valueOf(currentNeutralScaleFactor));
                    else
                        bd = bd.multiply(BigDecimal.valueOf(currentScaleFactor));

                    if (loadProfile) {
                        if (gekv2.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
                            bd = bd.multiply(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
                            if (gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                                bd = bd.divide(BigDecimal.valueOf((long)gekv2.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
                        }
                    }
                    if (loadProfile)
                        bd = bd.movePointLeft(9); // see kv2 doc
                    else
                        bd = bd.movePointLeft(11); // see kv2 doc
                    return bd;
                }

                case 16: // THDV IEEE
                case 17: { // THDI IEEE
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 24:// power factor computed using VA apparent id code 2
                case 25: { // power factor computed using VA phasor id code 3
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 33: { // Frequency
                    bd = bd.movePointLeft(2); // see kv2 doc
                    return bd;
                }

                case 34: { // Counter
                    return bd;
                }

                default:
                    throw new IOException("SourceInfo, basic2engineering(), invalid id code "+uomEntryBitField.getIdCode());
            }
        }

        throw new IOException("SourceInfo, basic2engineering(), no UOMEntryBitField, cannot calculate engineering value!");
    }

}
