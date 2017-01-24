/*
 * SourceUnits.java
 *
 * Created on 16 november 2005, 11:22
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.ansi.c12.tables.ElectricConstants;
import com.energyict.protocolimpl.ansi.c12.tables.UOMEntryBitField;
import com.energyict.protocolimpl.elster.a3.AlphaA3;

import java.io.IOException;
import java.math.BigDecimal;
/**
 *
 * @author Koen
 */
public class SourceInfo {

    AlphaA3 alphaA3;

    /** Creates a new instance of SourceUnits */
    public SourceInfo(AlphaA3 alphaA3) {
        this.alphaA3=alphaA3;
    }

    public ObisCodeDescriptor getObisCodeDescriptor(int dataControlEntryIndex) throws IOException {
        SourceDefinitionTable sourceDefinitionTable = alphaA3.getManufacturerTableFactory().getSourceDefinitionTable();
        UOMEntryBitField uomEntryBitField = sourceDefinitionTable.getSourceDefinitionEntries()[dataControlEntryIndex].getUomEntryBitField();
        UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
        return uom2ObisTranslator.getObisCodeDescriptor();
    }

    public Unit getUnit(int dataControlEntryIndex) throws IOException {
        SourceDefinitionTable sourceDefinitionTable = alphaA3.getManufacturerTableFactory().getSourceDefinitionTable();
        UOMEntryBitField uomEntryBitField = sourceDefinitionTable.getSourceDefinitionEntries()[dataControlEntryIndex].getUomEntryBitField();
        UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
        return uom2ObisTranslator.getUnit();
    }

    public Unit getChannelUnit(int sourceIndex) throws IOException {
        SourceDefinitionTable sourceDefinitionTable = alphaA3.getManufacturerTableFactory().getSourceDefinitionTable();
        UOMEntryBitField uomEntryBitField = sourceDefinitionTable.getSourceDefinitionEntries()[sourceIndex].getUomEntryBitField();
        UOM2ObisTranslator uom2ObisTranslator = new UOM2ObisTranslator(uomEntryBitField);
        return uom2ObisTranslator.getUnit();
    }

    private BigDecimal apply10Scaler(BigDecimal bd, int scale) {
        if (scale > 0)
            return(bd.movePointRight(scale));
        else if (scale < 0)
            return(bd.movePointLeft((-1)*scale));
        else
            return bd;
    }

    public BigDecimal basic2engineering(BigDecimal bd, int index, boolean loadProfile, boolean energy) throws IOException {

//System.out.println("KV_DEBUG> bd="+bd+", index="+index+", energy="+energy);

        UOMEntryBitField uomEntryBitField=null;
        int sourceIndex=-1;

        if (loadProfile) {
            // source index is load profile channel
            sourceIndex = alphaA3.getStandardTableFactory().getLoadProfileControlTable().getLoadProfileSelectionSet1()[index].getLoadProfileSourceSelect();
        }
        else sourceIndex=index;

        SourceDefinitionTable sourceDefinitionTable = alphaA3.getManufacturerTableFactory().getSourceDefinitionTable();

        int multiplierSelect = sourceDefinitionTable.getSourceDefinitionEntries()[sourceIndex].getMultiplierSelect();
        uomEntryBitField = sourceDefinitionTable.getSourceDefinitionEntries()[sourceIndex].getUomEntryBitField();

        BigDecimal multiplier = null;
        BigDecimal ct= null;;
        BigDecimal vt= null;;

        // STEP 1
        // ST15[MT17.multiplierIndex].multiplier * MT17.getMultiplier()
        // ST15[MT17.multiplierIndex].multiplier forgotten in the "How to read an A3 meter" doc
        if ((loadProfile) && ((uomEntryBitField.getIdCode()>=0) || (uomEntryBitField.getIdCode()<=3))) { //(!sourceDefinitionTable.getSourceDefinitionEntries()[sourceIndex].isConstantST15Applied()) {
           multiplier = (BigDecimal)((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[multiplierSelect]).getMultiplier();
//System.out.println("STEP 1 multiplier="+multiplier);
        }
        else
           multiplier = BigDecimal.valueOf(1);
        multiplier = apply10Scaler(multiplier, uomEntryBitField.getMultiplier());
//System.out.println("STEP 1.5 multiplier="+multiplier);

        // STEP 2
        if (!((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[multiplierSelect]).getSet1Constants().isSetAppliedBit()) {
        	if (energy || uomEntryBitField.getIdCode()<15) {

            int scale = alphaA3.getManufacturerTableFactory().getFactoryDefaultMeteringInformation().getInstrumentationScale();
            ct = (BigDecimal)((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[multiplierSelect]).getSet1Constants().getRatioF1();
            ct = apply10Scaler(ct, scale);

            vt = (BigDecimal)((ElectricConstants)alphaA3.getStandardTableFactory().getConstantsTable().getConstants()[multiplierSelect]).getSet1Constants().getRatioP1();
            vt = apply10Scaler(vt, scale);
            multiplier = multiplier.multiply(ct);
            multiplier = multiplier.multiply(vt);
//System.out.println("STEP 2 multiplier="+multiplier + " CT: " + ct + " PT: " + vt);
        	}
        }

        // STEP 3
        BigDecimal externalMultiplier = BigDecimal.valueOf(alphaA3.getManufacturerTableFactory().getPrimaryMeteringInformation().getExternalMultiplier());
        externalMultiplier = apply10Scaler(externalMultiplier, alphaA3.getManufacturerTableFactory().getPrimaryMeteringInformation().getExternalMultiplierScaleFactor());
        multiplier = multiplier.multiply(externalMultiplier);
//System.out.println("STEP 3 multiplier="+multiplier + " external multiplier: " + externalMultiplier);

        // parse idCode, netflow & quadrants
        switch(uomEntryBitField.getIdCode()) {
            case 5: // Q(45)
            case 4: // Q(60)
            case 3: // VA phasor
            case 2: // VA apparent
            case 1: // var reactive
            case 0: { // W active
//System.out.println("bd="+bd);
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                    if (!energy)
                        bd = bd.multiply(BigDecimal.valueOf((3600/alphaA3.getProfileInterval()))); //,BigDecimal.ROUND_HALF_UP);
                }
//System.out.println("bd="+bd);

                bd = bd.movePointLeft(3); // k (kilo) values
//System.out.println("bd="+bd);
                //KV_TO_DO fout in metercat? decimal point for intervaldata?

                return bd;
            }

            case 8: { // RMS volts
            	if(ct!=null) {
            		multiplier = multiplier.divide(ct);
            	}
                return bd.multiply(multiplier);
            }

            case 10: { // RMS volts squared V²
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }

            case 12: { // RMS amps
            	if(vt!=null) {
            		multiplier = multiplier.divide(vt);
            	}
            	return bd.multiply(multiplier);
            }

            case 14: { // RMS amps squared A²
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }

            case 16: // THDV IEEE
            case 17: { // THDI IEEE
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }

            case 24:// power factor computed using VA apparent id code 2
            case 25: { // power factor computed using VA phasor id code 3
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }


            case 27: { // TODO WHAT IS 27?
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }
            case 33: { // Frequency
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }

            case 34: { // Counter
                bd = bd.multiply(multiplier);
                if (loadProfile) {
                    bd = applyDivisors(bd, index);
                }
                return bd;
            }

            default:
                throw new IOException("SourceInfo, basic2engineering(), invalid id code "+uomEntryBitField.getIdCode());
        }

    } // public BigDecimal basic2engineering(BigDecimal value, boolean loadProfile) throws IOException

    public BigDecimal applyDivisors(BigDecimal bd, int index) throws IOException {
        if (alphaA3.getStandardTableFactory().getActualLoadProfileTable().getLoadProfileSet().isScalarDivisorFlagSet1()) {
            bd = bd.multiply(BigDecimal.valueOf((long)alphaA3.getStandardTableFactory().getLoadProfileControlTable().getDivisorSet1()[index]));
            if (alphaA3.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index] != 1)
                bd = bd.divide(BigDecimal.valueOf((long)alphaA3.getStandardTableFactory().getLoadProfileControlTable().getScalarsSet1()[index]),BigDecimal.ROUND_HALF_UP);
        }
        return bd;
    }
}
