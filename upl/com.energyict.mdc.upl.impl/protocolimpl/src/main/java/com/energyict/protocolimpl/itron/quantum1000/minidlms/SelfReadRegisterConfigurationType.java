/*
 * Result.java
 *
 * Created on 8 december 2006, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum1000.minidlms;

import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SelfReadRegisterConfigurationType {
    
    private static final int INSTANTANEOUS_DEMAND=0;
    private static final int THERMAL_DEMAND=1;
    private static final int BLOCK_DEMAND=2;
    private static final int ENERGY=3;
    private static final int RESERVED_FOR_MULTIPLE_PEAKS_AND_MINIMUMS=4;
    private static final int RESERVED_FOR_POWER_QUALITY=5;
    private static final int RESERVED_FOR_HARMONICS=6;
    
    private int registerType; 
    private QuantityId quantityId;
    private int id2; // 16 bit TOU rate for registerType
    private int id3; // 16 bit reserved
    
    /** Creates a new instance of Result */
    public SelfReadRegisterConfigurationType(byte[] data,int offset) throws IOException {
        setRegisterType(ProtocolUtils.getInt(data,offset++, 1));
        setQuantityId(QuantityFactory.findQuantityId(ProtocolUtils.getInt(data,offset, 2)));
        offset+=2;
        setId2(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        setId3(ProtocolUtils.getInt(data,offset, 2));
        offset+=2;
        
    }
    
    public boolean isSelfReadDemandRegisterType() {
        return (getRegisterType()==INSTANTANEOUS_DEMAND) || (getRegisterType()==THERMAL_DEMAND) || (getRegisterType()==BLOCK_DEMAND);
    }
    
    public boolean isSelfReadEnergyRegisterType() {
        return (getRegisterType()==ENERGY);
    }
    
    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("SelfReadRegisterConfigurationType:\n");
        strBuff.append("   id2="+getId2()+"\n");
        strBuff.append("   id3="+getId3()+"\n");
        strBuff.append("   quantityId="+getQuantityId()+"\n");
        strBuff.append("   registerType="+getRegisterType()+"\n");
        return strBuff.toString();
    }
    
    static public int size() {
        return 7;
    }

    public static int getINSTANTANEOUS_DEMAND() {
        return INSTANTANEOUS_DEMAND;
    }

    public static int getTHERMAL_DEMAND() {
        return THERMAL_DEMAND;
    }

    public static int getBLOCK_DEMAND() {
        return BLOCK_DEMAND;
    }

    public static int getENERGY() {
        return ENERGY;
    }

    public int getRegisterType() {
        return registerType;
    }

    public void setRegisterType(int registerType) {
        this.registerType = registerType;
    }

    public QuantityId getQuantityId() {
        return quantityId;
    }

    public void setQuantityId(QuantityId quantityId) {
        this.quantityId = quantityId;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public int getId3() {
        return id3;
    }

    public void setId3(int id3) {
        this.id3 = id3;
    }
    

    
}
