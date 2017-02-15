/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * UOMEntryBitField.java
 *
 * Created on 28 november 2005, 14:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.mdc.common.Unit;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;


/**
 *
 * @author Koen
 */
public class UOMEntryBitField {

    private int idCode; // bit 0..7

    private static final Unit[] units = {Unit.get("W"),     //  0 active power
                                         Unit.get("var"),   //  1 reactive power
                                         Unit.get("VA"),    //  2 apparent power
                                         Unit.get("VA"),    //  3 phasor power - VA = sqrt(W2+var2)
                                         Unit.get("var"),   //  4 quantity power Q(60)
                                         Unit.get("var"),   //  5 quantity power Q(45)
                                         Unit.get(""),Unit.get(""), //  6..7 reserved
                                         Unit.get("V"),     //  8 RMS volts
                                         Unit.get("V"),     //  9 average volts (average of |V|)
                                         Unit.get("V2"),    // 10 RMS volt square
                                         Unit.get("V"),     // 11 instantaneous volt
                                         Unit.get("A"),     // 12 RMS amp
                                         null,      // 13 entry does not exist in the C12.19 table 12
                                         Unit.get("A2"),    // 14 RMS amps squared
                                         Unit.get("A"),     // 15 instantaneous current
                                         Unit.get("THDVIEEE"),  // 16 total harminic distortion Volt (IEEE)
                                         Unit.get("THDIIEEE"),  // 17 total harminic distortion Current (IEEE)
                                         Unit.get("THDVIC"),  // 18 total harminic distortion Volt (IC)
                                         Unit.get("THDIIC"),  // 19 total harminic distortion Current (IC)
                                         //  phase angles
                                         Unit.get("°"),     // 20 V-VA, voltage phase angle
                                         Unit.get("°"),     // 21 Vx-Vy, where x and y are phases in phase
                                         Unit.get("°"),     // 22 I-VA, current phase angle
                                         Unit.get("°"),     // 23 Ix-Iy, where x and y are phases defined in phase
                                         Unit.get(""),      // 24 Power factor computed using apparent power
                                         Unit.get(""),      // 25 Power factor computed using phasor power
                                         Unit.get(""),      // 26 reserved
                                         Unit.get(""),      // 27 reserved
                                         Unit.get(""),      // 28 reserved
                                         // time
                                         Unit.get(""),      // 29 time of day
                                         Unit.get(""),      // 30 date
                                         Unit.get(""),      // 31 time of day and date
                                         Unit.get(""),      // 32 interval timer
                                         Unit.get("Hz"),    // 33 frequency
                                         Unit.get(""),      // 34 counter
                                         Unit.get(""),      // 35 Sense input (T/F)
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 36..39 reserved
                                         Unit.get(""),      // 40 voltage sag
                                         Unit.get(""),      // 41 voltage swells
                                         Unit.get(""),      // 42 power outage
                                         Unit.get(""),      // 43 voltage excursion low
                                         Unit.get(""),      // 44 voltage excursion high
                                         Unit.get("V"),     // 45 normal voltage level
                                         Unit.get(""),      // 46 voltage unbalance
                                         Unit.get(""),      // 47 voltage THD excess
                                         Unit.get(""),      // 48 current THD excess
                                         Unit.get(""),      // 49 reserved

                                         Unit.get(""),      // 50 power outages
                                         Unit.get(""),      // 51 number of demand resets
                                         Unit.get(""),      // 52 number of times programmed
                                         Unit.get("min"),      // 53 number of minutes on battery carryover

                                         // gas industry units
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 54..63 reserved
                                         Unit.get("m3/h"),  // 64 cubic meter gas (volume uncorrected meter index reading) per hour
                                         Unit.get("m3/h"),  // 65 cor cubic meter gas (volume corrected to base conditions) per hour
                                         Unit.get("m3/h"),  // 66 cor cubic meter gas (volume corrected to pressure base, without supercompressibility) per hour
                                         Unit.get("ft3/h"),  // 67 cubic feet gas (volume uncorrected meter index reading) per hour
                                         Unit.get("ft3/h"),  // 68 cor cubic feet gas (volume corrected to base conditions) per hour
                                         Unit.get("ft3/h"),  // 69 cor cubic feet gas (volume corrected to pressure base, without supercompressibility) per hour
                                         Unit.get("°C"), // 70 dry bulb temp
                                         Unit.get("°C"), // 71 wet bulb temp
                                         Unit.get("°F"), // 72 dry bulb temp
                                         Unit.get("°F"), // 73 wet bulb temp
                                         Unit.get("°K"), // 74 dry bulb temp
                                         Unit.get("°K"), // 75 wet bulb temp
                                         Unit.get("J/h"), // 76 joules per hour
                                         Unit.get(""),   // 77 Therm per hour
                                         Unit.get("Pa"),   // 78 static Pascal
                                         Unit.get("Pa"),   // 79 Differential Pascal
                                         Unit.get("lb/in2"), // 80 static pound per square inch
                                         Unit.get("lb/in2"), // 81 differential pound per square inch
                                         Unit.get("g/cm2"), // 82 gram cm2
                                         Unit.get("mHg"), // 83 meter kwikkolom
                                         Unit.get("inHg"), // 84 inch kwikkolom
                                         Unit.get("inH2O"), // 85 meter waterkolom
                                         Unit.get("bar"), // 86 Bar
                                         Unit.get("%"), // 87 percent relative humidity
                                         Unit.get("ppm"), // 88 parts per million odorant
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 89..127 reserved
                                         // water industry units
                                         Unit.get("m3/h"), // 128 cubic meter liquid per hour
                                         Unit.get("ft3/h"), // 129 cubic feet liquid per hour
                                         Unit.get("gal/h"), // 130 US gallons per hour
                                         Unit.get("gal/h"), // 131 IMP gallons per hour
                                         Unit.get("ft/h"), // 132 acre feet per hour
                                         Unit.get("ppm"), // 133 parts per million lead
                                         Unit.get(""),  // 134 Turbidity
                                         Unit.get("ppm"),  // 135 ppm chlorine
                                         Unit.get(""),  // 136 PH factor
                                         Unit.get(""),  // 137 corrosion
                                         Unit.get(""),  // 138 ionization
                                         Unit.get("ppm"),  // 139 ppm S0_2
                                         Unit.get("l"), // 140 liters
                                         Unit.get("ft3"), // 141 cubic feet liquid
                                         Unit.get("lb/ft2"),  // 142 pounds per square foot differential
                                         Unit.get(""), // 143 inches of water
                                         Unit.get(""), // 144 feet of water
                                         Unit.get(""), // 145 Atmospheres
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 146..189 reserved
                                         // generic industry units
                                         Unit.get(""), // 190 local currency
                                         Unit.get("in"), // 191 inches
                                         Unit.get("ft"), // 192 foot
                                         Unit.get("m"),  // 193 meter
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),   // 194..200
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 201..210
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 211..220
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 221..230
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""), // 231..240
                                         Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get(""),Unit.get("")}; // 241..255
    private int timeBase; // bit 8..10
    private int multiplier; // bit 11..13
    private boolean q1Accountability; // bit 14
    private boolean q2Accountability; // bit 15
    private boolean q3Accountability; // bit 16
    private boolean q4Accountability; // bit 17
    private boolean netFlowAccountability; // bit 18
    private int segmentation; // bit 19..21
    private boolean harmonic; // bit 22
    // reserved // bit 23..30
    private boolean nfs; // bit 31

    private long tableValue;

    public UOMEntryBitField() {

    }


    /** Creates a new instance of UOMEntryBitField */
    public UOMEntryBitField(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        tableValue = C12ParseUtils.getLong(data,offset,4, dataOrder);
        setIdCode((int)(tableValue & 0x000000FFL));
        setTimeBase((int)((tableValue>>8) & 0x00000007L));
        setMultiplier((int)((tableValue>>11) & 0x00000007L));
        setQ1Accountability(((tableValue & 0x00004000L)==0x00004000L));
        setQ2Accountability(((tableValue & 0x00008000L)==0x00008000L));
        setQ3Accountability(((tableValue & 0x00010000L)==0x00010000L));
        setQ4Accountability(((tableValue & 0x00020000L)==0x00020000L));
        setNetFlowAccountability(((tableValue & 0x00040000L)==0x00040000L));
        setSegmentation((int)((tableValue>>19) & 0x00000007L));
        setHarmonic(((tableValue & 0x00400000L)==0x00400000L));
        setNfs(((tableValue & 0x80000000L)==0x80000000L));
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("UOMEntryBitField:");
        strBuff.append(",  tableValue=0x"+Long.toHexString(getTableValue()));
        strBuff.append(",  idCode ("+units[idCode]+")="+idCode);
        strBuff.append(", timeBase="+timeBase);
        strBuff.append(", multiplier="+multiplier);
        strBuff.append(", timeBase="+timeBase);
        strBuff.append(", q1Accountability="+q1Accountability+", q2Accountability="+q2Accountability+", q3Accountability="+q3Accountability+", q4Accountability="+q4Accountability);
        strBuff.append(", netFlowAccountability="+netFlowAccountability);
        strBuff.append(", segmentation="+segmentation);
        strBuff.append(", harmonic="+harmonic);
        strBuff.append(", nfs="+nfs+"\n");
        return strBuff.toString();
    }

    static public int getSize() throws IOException {
        return 4;
    }

    public Unit getUnit() {
        return units[getIdCode()];
    }

    public int getIdCode() {
        return idCode;
    }

    public void setIdCode(int idCode) {
        this.idCode = idCode;
    }

    public int getTimeBase() {
        return timeBase;
    }

    public void setTimeBase(int timeBase) {
        this.timeBase = timeBase;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = multiplier;
    }

    public boolean isQ1Accountability() {
        return q1Accountability;
    }

    public void setQ1Accountability(boolean q1Accountability) {
        this.q1Accountability = q1Accountability;
    }

    public boolean isQ2Accountability() {
        return q2Accountability;
    }

    public void setQ2Accountability(boolean q2Accountability) {
        this.q2Accountability = q2Accountability;
    }

    public boolean isQ3Accountability() {
        return q3Accountability;
    }

    public void setQ3Accountability(boolean q3Accountability) {
        this.q3Accountability = q3Accountability;
    }

    public boolean isQ4Accountability() {
        return q4Accountability;
    }

    public void setQ4Accountability(boolean q4Accountability) {
        this.q4Accountability = q4Accountability;
    }

    public boolean isNetFlowAccountability() {
        return netFlowAccountability;
    }

    public void setNetFlowAccountability(boolean netFlowAccountability) {
        this.netFlowAccountability = netFlowAccountability;
    }

    public int getSegmentation() {
        return segmentation;
    }

    public void setSegmentation(int segmentation) {
        this.segmentation = segmentation;
    }

    public boolean isHarmonic() {
        return harmonic;
    }

    public void setHarmonic(boolean harmonic) {
        this.harmonic = harmonic;
    }

    public boolean isNfs() {
        return nfs;
    }

    public void setNfs(boolean nfs) {
        this.nfs = nfs;
    }

    public long getTableValue() {
        return tableValue;
    }

    public void setTableValue(long tableValue) {
        this.tableValue = tableValue;
    }
}
