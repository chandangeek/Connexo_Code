/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 28 oktober 2005, 17:28
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.TableFactory;
import com.energyict.protocolimpl.ansi.c12.tables.UOMEntryBitField;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SourceDefinitionEntry {


    private UOMEntryBitField uomEntryBitField;
    
    private int uomCode; // 1 byte As defined by C12.19 in ST-12, the unit of measure code of the physical quantity of interest.
    private int flow; // 1 byte The Quadrant Accountability fields indicate the allowable quadrants for this source. 
              // A source may be listed as accountable in multiple quadrants. For example, WHd would be accountable to Q1 and Q4.
                             // b0: Q1_ACCOUNTABILITY Set indicates that the source lies in Quadrant 1. (WHd, VARHd)
                             // b1: Q2_ACCOUNTABILITY Set indicates that source lies in Quadrant 2. (WHr, VARHd)
                             // b2: Q3_ACCOUNTABILITY Set indicates that source lies in Quadrant 3. (WHr, VARHr)
                             // b3: Q4_ACCOUNTABILITY Set indicates that source lies in Quadrant 4. (WHd, VARHr)
                             // b4: NET_FLOW_ACCOUNTABILITY 1 = delivered + received, 0 = delivered + received
                             // b5-7: SEGMENTATION
              // Define the phase related information for the source. For instrumentation sources, 
              // the meter uses this field to determine the phase to request from the DSP.
                             // 0 = system (total) or not phase related
                             // 1 = A-B (corresponds to phase A instrumentation)
                             // 2 = B-C (corresponds to phase C instrumentation)
                             // 3 = C-A (not valid for instrumentation requests)
                             // 4 = neutral (i.e. neutral current)
                             // 5 = A-neutral
                             // 6 = B-neutral
                             // 7 = C-neutral
    private int usage; // 1 byte This bit field identifies the metering functions or applications that are allowed to
               // use this source. A set bit indicates the source may be selected as an input source for the corresponding function.
                              // b0 = BILLING_SUMMATION _USE
                              // b1 = BILLING_DEMAND_USE
                              // b2 = PROFILE_USE
                              // b3 = POWER_QUALITY_USE
                              // b4 = WAVE_FORM_USE
                              // b5-7 = Not used by the meter = 0
                              // The meter does not use this field. It is information carried to support external software.
    private int harmonicInformation; // 1 byte b0-5: HARMONIC_DEF
                             // 0 = entire signal unfiltered
                             // 1 = 1st harmonic (fundamental)
                             // 2 = 2nd harmonic
                             // 15 = 15th harmonic
                             // 63 = non-specific harmonic related information. (Example: %THD)
                             // b6: CONSTANT_TO_BE_APPLIED = 0  0 = The multiplier entry in ST-15 has been applied to the source 
                             //                                 1 = The multiplier entry in ST-15 has not been applied to the source 
                             // b7: NFS = 0
    private int scale; // 1 byte b0-4 SCALE_FACTOR
                              // The scale factor to be applied to the reported value after delivery of the item. 
                              // For example, if the scale factor is set to 10-6, and the source is a WH summation, the value contained
                              // In ST-23 has units of ?WH. This is a 5 bit signed number.
                              // Yielding the 10x exponent in the range 10-16 to 1015.
                              // For non-power factor pulse sources, when MT-15 is written, the meter sets the scale factor using MT-15.
                              // Adjusted_Ke_Scale_Factor.
                              // b5-7 unused = 0
    private int demandSelect; // 1 byte The meter does not use this field. The first Dmd Interval Config entry in ST-13
                                               // is used during normal mode and the second first Dmd Interval Config entry in ST-13 is used during test mode.
    private int multiplierSelect; // 1 byte Index into ST-15 to select multiplier.
                                                             // Index 0= Instrumentation Multipliers
                                                             // Index 1= Adjusted Ke Multipliers
                                                             // Index 2= Input Relay 1 Multipliers
                                                             // Index 3= Input Relay 2 Multipliers
                                                             // Index 4= Input Relay 3 Multipliers
                                                             // Index 5= Input Relay 4 Multipliers

    /** Creates a new instance of SourceDefinitionEntry */
    public SourceDefinitionEntry(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int dataOrder = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable().getDataOrder();
        setUomCode(C12ParseUtils.getInt(data,offset++));
        setFlow(C12ParseUtils.getInt(data,offset++));
        setUsage(C12ParseUtils.getInt(data,offset++));
        setHarmonicInformation(C12ParseUtils.getInt(data,offset++));
        
        data[offset] = (data[offset]&0x10)==0x10?(byte)(((int)data[offset]&0xFF) | 0xE0):(byte)(data[offset] & 0x1F); // 5 bit sign extention
        setScale((int)C12ParseUtils.getExtendedLong(data,offset++));
        
        setDemandSelect(C12ParseUtils.getInt(data,offset++));
        
        // KV_TO_DO where to use this?
        setMultiplierSelect(C12ParseUtils.getInt(data,offset++));
        
        setUomEntryBitField(new UOMEntryBitField());
        getUomEntryBitField().setIdCode(getUomCode());
        getUomEntryBitField().setQ1Accountability((getFlow()&0x01)==0x01);
        getUomEntryBitField().setQ2Accountability((getFlow()&0x02)==0x02);
        getUomEntryBitField().setQ3Accountability((getFlow()&0x04)==0x04);
        getUomEntryBitField().setQ4Accountability((getFlow()&0x08)==0x08);
        getUomEntryBitField().setNetFlowAccountability((getFlow()&0x10)==0x10);
        getUomEntryBitField().setSegmentation((getFlow()&0xE0)>>5);
        getUomEntryBitField().setNfs(false);
        getUomEntryBitField().setMultiplier(getScale());
        getUomEntryBitField().setHarmonic((getHarmonicInformation()&0x1F)!=0);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SourceDefinitionEntry:\n");
        strBuff.append("   uomCode="+getUomCode()+"\n");
        strBuff.append("   flow=0x"+Integer.toHexString(getFlow())+"\n");
        strBuff.append("   usage=0x"+Integer.toHexString(getUsage())+"\n");
        strBuff.append("   harmonicInformation=0x"+Integer.toHexString(getHarmonicInformation())+"\n");
        strBuff.append("   scale="+getScale()+"\n");
        strBuff.append("   demandSelect="+getDemandSelect()+"\n");
        strBuff.append("   multiplierSelect="+getMultiplierSelect()+"\n");
        strBuff.append("   uomEntryBitField="+getUomEntryBitField()+"\n");
        return strBuff.toString();
    }
    
    static public int getSize(TableFactory tableFactory) throws IOException {
        return 7;
    }   

    public int getUomCode() {
        return uomCode;
    }

    public void setUomCode(int uomCode) {
        this.uomCode = uomCode;
    }

    public int getFlow() {
        return flow;
    }

    public void setFlow(int flow) {
        this.flow = flow;
    }

    public int getUsage() {
        return usage;
    }

    public void setUsage(int usage) {
        this.usage = usage;
    }

    public int getHarmonicInformation() {
        return harmonicInformation;
    }

    public void setHarmonicInformation(int harmonicInformation) {
        this.harmonicInformation = harmonicInformation;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

    public int getDemandSelect() {
        return demandSelect;
    }

    public void setDemandSelect(int demandSelect) {
        this.demandSelect = demandSelect;
    }

    public int getMultiplierSelect() {
        return multiplierSelect;
    }

    public void setMultiplierSelect(int multiplierSelect) {
        this.multiplierSelect = multiplierSelect;
    }

    public UOMEntryBitField getUomEntryBitField() {
        return uomEntryBitField;
    }

    public void setUomEntryBitField(UOMEntryBitField uomEntryBitField) {
        this.uomEntryBitField = uomEntryBitField;
    }
    
    public boolean isConstantST15Applied() {
        return (getHarmonicInformation()&0x40) == 0x40;
    }
}
