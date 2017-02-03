/*
 * MassMemoryProgramTable.java
 *
 * Created on 14 september 2006, 15:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.fulcrum.basepages;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author Koen
 */
public class MassMemoryProgramTable {
    /*
    energyRegisterNumber:
    0 Wh
    1 Lagging VARh
    2 VAh
    3 Qh
    4 Lead or Total VARh
    5 Volt-squared hour
    6 Amp hour
    */
    Unit[] units = new Unit[]{Unit.get("Wh"),Unit.get("varh"),Unit.get("VAh"),Unit.get("varh"),Unit.get("varh"),Unit.get(BaseUnit.VOLTSQUAREHOUR),Unit.get(BaseUnit.AMPEREHOUR)};
    
    String[] obisCFieldDescriptions=new String[]{"active import","reactive Q1 (lagging during active import)","apparent","reactive import","reactive Q4 (leading during active import)","","V2","Current any phase"};
    private int energyRegisterNumber;
    private BigDecimal pulseWeight;
    // 10 bytes reserved
    
    
    /** Creates a new instance of MassMemoryProgramTable */
    public MassMemoryProgramTable(byte[] data, int offset) throws IOException {
        setEnergyRegisterNumber(ProtocolUtils.getInt(data,offset,2));
        offset+=2;
        setPulseWeight(new BigDecimal(""+Float.intBitsToFloat(ProtocolUtils.getInt(data,offset,4))));
        offset+=4;
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MassMemoryProgramTable:\n");
        try {
           strBuff.append("   energyRegisterNumber="+getEnergyRegisterNumber()+", "+getEnergyRegisterObisCFieldDescription()+", "+getEnergyRegisterUnit()+"\n");
        }
        catch(IOException e) {
           strBuff.append("   energyRegisterNumber="+getEnergyRegisterNumber()+", "+e.toString()+"\n");
        }
        strBuff.append("   pulseWeight="+getPulseWeight()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 16;
    }
    
    public int getEnergyRegisterObisCField() throws IOException {
        if (getEnergyRegisterNumber()>=RegisterFactory.obisCFields.length)
            throw new IOException("MassMemoryProgramTable, error, invalid energyRegisterNumber "+getEnergyRegisterNumber());
        return RegisterFactory.obisCFields[getEnergyRegisterNumber()];
        
    }
    
    public Unit getEnergyRegisterUnit() throws IOException {
        if (getEnergyRegisterNumber()>=RegisterFactory.obisCFields.length)
            throw new IOException("MassMemoryProgramTable, error, invalid energyRegisterNumber "+getEnergyRegisterNumber());
        return units[getEnergyRegisterNumber()];
        
    }
    
    public String getEnergyRegisterObisCFieldDescription() throws IOException {
        if (getEnergyRegisterNumber()>=RegisterFactory.obisCFields.length)
            throw new IOException("MassMemoryProgramTable, error, invalid energyRegisterNumber "+getEnergyRegisterNumber());
        return obisCFieldDescriptions[getEnergyRegisterNumber()];
        
    }
    
    public int getEnergyRegisterNumber() {
        return energyRegisterNumber;
    }
    
    public void setEnergyRegisterNumber(int energyRegisterNumber) {
        this.energyRegisterNumber = energyRegisterNumber;
    }
    
    public BigDecimal getPulseWeight() {
        return pulseWeight;
    }
    
    public void setPulseWeight(BigDecimal pulseWeight) {
        this.pulseWeight = pulseWeight;
    }
    
}
