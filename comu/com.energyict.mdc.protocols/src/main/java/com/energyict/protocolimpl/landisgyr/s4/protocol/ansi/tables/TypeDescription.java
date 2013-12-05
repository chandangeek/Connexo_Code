/*
 * TypeDescription.java
 *
 * Created on july 2006
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.landisgyr.s4.protocol.ansi.tables;

import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TypeDescription {

    private int classType; // : UINT(0..4);
    //Reserved : FILL(5);
    private boolean twoAndHalfElementFlag; // : BOOL(6);
    private boolean twoElementFlag; // : BOOL(7);

    /** Creates a new instance of TypeDescription */
    public TypeDescription(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setClassType((int)data[offset]&0x1F);
        setTwoAndHalfElementFlag(((data[offset]>>6)&0x01) == 0x01);
        setTwoElementFlag(((data[offset]>>7)&0x01) == 0x01);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("TypeDescription:\n");
        strBuff.append("   classType="+getClassType()+"\n");
        strBuff.append("   twoAndHalfElementFlag="+isTwoAndHalfElementFlag()+"\n");
        strBuff.append("   twoElementFlag="+isTwoElementFlag()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public int getClassType() {
        return classType;
    }

    public void setClassType(int classType) {
        this.classType = classType;
    }

    public boolean isTwoAndHalfElementFlag() {
        return twoAndHalfElementFlag;
    }

    public void setTwoAndHalfElementFlag(boolean twoAndHalfElementFlag) {
        this.twoAndHalfElementFlag = twoAndHalfElementFlag;
    }

    public boolean isTwoElementFlag() {
        return twoElementFlag;
    }

    public void setTwoElementFlag(boolean twoElementFlag) {
        this.twoElementFlag = twoElementFlag;
    }

}
