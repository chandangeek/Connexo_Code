/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UOM2ObisTranslator.java
 *
 * Created on 30 november 2005, 11:20
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.ansi.c12.tables.UOMEntryBitField;

import java.io.IOException;

/**
 *
 * @author koen
 */
public class UOM2ObisTranslator {

    UOMEntryBitField uomEntryBitField;

    public UOM2ObisTranslator(UOMEntryBitField uomEntryBitField) {
        this.uomEntryBitField=uomEntryBitField;
    }

    public Unit getUnit() throws IOException {
        Unit unit=null;
        // parse idCode, netflow & quadrants
        switch(uomEntryBitField.getIdCode()) {
            case 0: { // W active
                unit = Unit.get("kW");
            } break;

            case 1: { // var reactive
                unit = Unit.get("kvar");
            } break;

            case 2: { // VA apparent
                unit = Unit.get("kVA");
            } break;

            case 3: { // VA phasor
                unit = Unit.get("kVA");
            } break;

            case 4: { // Quantity power Q(60)
                unit = Unit.get("kQ");
            } break;

            case 5: { // Quantity power Q(45)
                unit = Unit.get("kQ");
            } break;

            case 8: { // RMS volts
                unit = Unit.get("V");
            } break;

            case 10: { // RMS volts squared V²
                unit = Unit.get("V2");
            } break;

            case 12: { // RMS amps
                unit = Unit.get("A");
            } break;

            case 14: { // RMS amps squared A²
                unit = Unit.get("A2");
            } break;

            case 16: { // THDV IEEE
                unit = Unit.get("THDVIEEE");
            } break;

            case 17: { // THDI IEEE
                unit = Unit.get("THDIIEEE");
            } break;

            case 20: {
            	unit = Unit.get("\u00B0");
            }break;

            case 22: {
                unit = Unit.get("\u00B0");
            }break;

            case 24: { // power factor computed using VA apparent id code 2
                unit = Unit.get("");
            } break;

            case 25: { // power factor computed using VA phasor id code 3
                unit = Unit.get("");
            } break;

//            case 27: {
//            	unit = Unit.get("");
//            }break;

            case 33: { // Frequency
                unit = Unit.get("Hz");
            } break;

            case 34: { // Counter
                unit = Unit.get("");
            } break;

            default:
                throw new IOException("UOM2ObisTranslator, getUnit(), invalid id code "+uomEntryBitField.getIdCode());
        }

        return unit;
    }

    private int getSegmentationCFieldOffset(int segmentation) throws IOException {
        if (segmentation == 0)      // all phases
            return 0;
        else if (segmentation == 5) // phase A
            return 20;
        else if (segmentation == 6) // phase B
            return 40;
        else if (segmentation == 7) // phase C
            return 60;
        else if (segmentation == 1) // phase A to B
            return 148;
        else if (segmentation == 2) // phase B to C
            return 168;
        else if (segmentation == 3) // phase C to A
            return 188;
        else if (segmentation == 4) // neutral to ground
            return 128;
        else throw new IOException("UOM2ObisTranslator, getSegmentationCFieldOffset(), invalid segmentation "+segmentation);
    }


    private String getSegmentationDescription(int segmentation) throws IOException {
        if (segmentation == 0)
            return "";
        else if (segmentation == 5)
            return "Phase A";
        else if (segmentation == 6)
            return "Phase B";
        else if (segmentation == 7)
            return "Phase C";
        else if (segmentation == 1)
            return "Phase A-B";
        else if (segmentation == 2)
            return "Phase B-C";
        else if (segmentation == 3)
            return "Phase C-A";
        else if (segmentation == 4)
            return "Neutral";
        else throw new IOException("UOM2ObisTranslator, getSegmentationCFieldOffset(), invalid segmentation "+segmentation);
    }

    private String getSegmentationAndHarmonicsDescription() throws IOException {
        String description=null;
        boolean harmonic = uomEntryBitField.isHarmonic();
        int segmentation = uomEntryBitField.getSegmentation();
        if ((getSegmentationCFieldOffset(segmentation)>=128) && !harmonic)
            description=("power quality specifics entire unfiltered signal");
        else if ((getSegmentationCFieldOffset(segmentation)>=128) && harmonic)
            description=("power quality specifics on a harmonic of the signal");
        else if (harmonic)
            description=("power & energy on a harmonic of the signal");
        else if (!harmonic)
            description=("power & energy entire unfiltered signal");

        return description+", "+getSegmentationDescription(segmentation);
    }

    private String getQuadrantDescription() throws IOException {
        String description;
        boolean netFlow = uomEntryBitField.isNetFlowAccountability();
        boolean q1 = uomEntryBitField.isQ1Accountability();
        boolean q2 = uomEntryBitField.isQ2Accountability();
        boolean q3 = uomEntryBitField.isQ3Accountability();
        boolean q4 = uomEntryBitField.isQ4Accountability();
        boolean imported = q1 && q4;
        boolean exported = q2 && q3;
        boolean importMinusExport = q1 && q2 && q3 && q4 && netFlow;
        boolean importPlusExport = q1 && q2 && q3 && q4 && !netFlow;
        if (importMinusExport) {
            description = "(Q1+Q4)-(Q2+Q3)";
        }
        else if (importPlusExport) {
            description = "(Q1+Q4)+(Q2+Q3)";
        }
        else if (imported) {
            description = "(Q1+Q4)";
        }
        else if (exported) {
            description = "(Q2+Q3)";
        }
        else if (q1) {
            description = "(Q1)";
        }
        else if (q2) {
            description = "(Q2)";
        }
        else if (q3) {
            description = "(Q3)";
        }
        else if (q4) {
            description = "(Q4)";
        }
        else description="";
        return description;
    }

    public ObisCodeDescriptor getObisCodeDescriptor() throws IOException {

        StringBuffer strBuff = new StringBuffer();

        int idCode = uomEntryBitField.getIdCode();
        boolean harmonic = uomEntryBitField.isHarmonic();
        int segmentation = uomEntryBitField.getSegmentation();
        boolean netFlow = uomEntryBitField.isNetFlowAccountability();
        boolean q1 = uomEntryBitField.isQ1Accountability();
        boolean q2 = uomEntryBitField.isQ2Accountability();
        boolean q3 = uomEntryBitField.isQ3Accountability();
        boolean q4 = uomEntryBitField.isQ4Accountability();
        boolean imported = q1 && q4;
        boolean exported = q2 && q3;
        boolean importMinusExport = q1 && q2 && q3 && q4 && netFlow;
        boolean importPlusExport = q1 && q2 && q3 && q4 && !netFlow;
        int cField=0;
        boolean voltageAngle = false;

        // parse idCode, netflow & quadrants
        switch(uomEntryBitField.getIdCode()) {
            case 0: { // W active
                strBuff.append("W active");
                if (importMinusExport) {
                    cField=15;
                }
                else if (importPlusExport) {
                    cField=16;
                }
                else if (imported) {
                    cField=1;
                }
                else if (exported) {
                    cField=2;
                }
                else if (q1) {
                    cField=17;
                }
                else if (q2) {
                    cField=18;
                }
                else if (q3) {
                    cField=19;
                }
                else if (q4) {
                    cField=20;
                }
            } break;

            case 1: { // var reactive
                strBuff.append("var reactive");
                if (importMinusExport) {
                    cField=128;
                }
                else if (importPlusExport) {
                    cField=129;
                }
                else if (imported) {
                    cField=3;
                }
                else if (exported) {
                    cField=4;
                }
                else if (q1) {
                    cField=5;
                }
                else if (q2) {
                    cField=6;
                }
                else if (q3) {
                    cField=7;
                }
                else if (q4) {
                    cField=8;
                }

            } break;

            case 2: { // VA apparent
                strBuff.append("VA apparent");
                if (importMinusExport) {
                    cField=130;
                }
                else if (importPlusExport) {
                    cField=131;
                }
                else if (imported) {
                    cField=9;
                }
                else if (exported) {
                    cField=10;
                }
                else if (q1) {
                    cField=132;
                }
                else if (q2) {
                    cField=133;
                }
                else if (q3) {
                    cField=134;
                }
                else if (q4) {
                    cField=135;
                }
            } break;

            case 3: { // VA phasor
                strBuff.append("VA phasor");
                if (importMinusExport) {
                    cField=136;
                }
                else if (importPlusExport) {
                    cField=137;
                }
                else if (imported) {
                    cField=138;
                }
                else if (exported) {
                    cField=139;
                }
                else if (q1) {
                    cField=140;
                }
                else if (q2) {
                    cField=141;
                }
                else if (q3) {
                    cField=142;
                }
                else if (q4) {
                    cField=143;
                }
            } break;

            case 4: { // Quantity power Q(60)
                strBuff.append("Quantity power Q(60)");
                cField=149;
            } break;

            case 5: { // Quantity power Q(45)
                strBuff.append("Quantity power Q(45)");
                cField=150;
            } break;

            case 8: { // RMS volts
                strBuff.append("RMS volts");
                cField=12;
            } break;

            case 10: { // RMS volts squared V²
                strBuff.append("RMS volts squared");
                cField=144;
            } break;

            case 12: { // RMS amps
                strBuff.append("RMS amps");
                cField=11;
            } break;

            case 14: { // RMS amps squared A²
                strBuff.append("RMS amps squared");
                cField=145;
            } break;

            case 16: { // THDV IEEE
                strBuff.append("THDV IEEE");
                cField=146;
            } break;

            case 17: { // THDI IEEE
                strBuff.append("THDI IEEE");
                cField=147;
            } break;

            case 20: { // V-VA Voltage Phase Angle
                strBuff.append("V-VA Voltage Phase Angle");
                cField=81;
                voltageAngle = true;
            } break;

            case 22: { // I-VA Current Phase Angle
                strBuff.append("I-VA Current Phase Angle");
                cField=81;
            } break;

            case 24: { // power factor computed using VA apparent id code 2
                strBuff.append("Power factor using VA apparent");
                cField=13;
            } break;

            case 25: { // power factor computed using VA phasor id code 3
                strBuff.append("Power factor using VA phasor");
                cField=148;
            } break;

//            case 27: {
//            	strBuff.append("UNKNOWN");
//                cField=1;
//            }break;


            case 33: { // Frequency
                strBuff.append("Frequency");
                cField=14;
            } break;

            case 34: { // Counter
                strBuff.append("Counter");
                cField=82;
            } break;

            default:
                throw new IOException("UOM2ObisTranslator, getCFieldDescriptor(), invalid id code "+uomEntryBitField.getIdCode());
        }

        // apply harmonic & segmentation
        int bField=1;
        if (harmonic)
            bField=2;

        int segmentationOffset=0;
        if (getSegmentationCFieldOffset(segmentation)>=128) {
            bField+=128;
            segmentationOffset = getSegmentationCFieldOffset(segmentation)-128;
        }
        else segmentationOffset = getSegmentationCFieldOffset(segmentation);

        strBuff.append(", "+getQuadrantDescription()+", "+getSegmentationAndHarmonicsDescription());

        ObisCodeDescriptor ocd;
        if(cField != 81) {
        	//cField set to 81 indicates angles and the phases are not represented here but in the E field
        	cField += segmentationOffset;
        	ocd = new ObisCodeDescriptor(bField,cField, strBuff.toString());
        } else {
        	ocd = new ObisCodeDescriptor(bField,cField, strBuff.toString());
        	ocd.setCurrentDField(7);
        	ocd.setCurrentEField(getEfield(voltageAngle, segmentation));
        }


		return ocd;

    } // private ObisCodeDescriptor buildObisCodefields() throws IOException

	private int getEfield(boolean voltageAngle, int segmentation) throws IOException {
		if (voltageAngle) {
			if (segmentation == 5) // phase A
				return 10;
			else if (segmentation == 6) // phase B
				return 21;
			else if (segmentation == 7) // phase C
				return 2;
		}else {
			if (segmentation == 5) // phase A
				return 4;
			else if (segmentation == 6) // phase B
				return 15;
			else if (segmentation == 7) // phase C
				return 26;
		}
		throw new IOException("UOM2ObisTranslator, getEfield, invalid segmentation "+segmentation);
	}
}
