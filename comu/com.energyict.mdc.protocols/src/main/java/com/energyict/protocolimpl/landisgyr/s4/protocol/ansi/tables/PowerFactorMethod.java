/*
 * RecordTemplate.java
 *
 * Created on 4 juli 2006, 9:03
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
public class PowerFactorMethod {

    private boolean kVAhTDPowerFactorFlag; // : BOOL(0);
    private boolean kVARhTDPowerFactorFlag; // : BOOL(1);
    private boolean kVAhRMSPowerFactorFlag; // : BOOL(2);
    // FILLER : FILL(3..7);

    /** Creates a new instance of RecordTemplate */
    public PowerFactorMethod(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int temp = (int)data[offset++]&0xFF;
        setKVAhTDPowerFactorFlag((temp & 0x01) == 0x01);
        setKVARhTDPowerFactorFlag(((temp & 0x02)>>1) == 0x01);
        setKVAhRMSPowerFactorFlag(((temp & 0x04)>>2) == 0x01);
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("PowerFactorMethod:\n");
        strBuff.append("   KVARhTDPowerFactorFlag="+isKVARhTDPowerFactorFlag()+"\n");
        strBuff.append("   KVAhRMSPowerFactorFlag="+isKVAhRMSPowerFactorFlag()+"\n");
        strBuff.append("   KVAhTDPowerFactorFlag="+isKVAhTDPowerFactorFlag()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public boolean isKVAhTDPowerFactorFlag() {
        return kVAhTDPowerFactorFlag;
    }

    private void setKVAhTDPowerFactorFlag(boolean kVAhTDPowerFactorFlag) {
        this.kVAhTDPowerFactorFlag = kVAhTDPowerFactorFlag;
    }

    public boolean isKVARhTDPowerFactorFlag() {
        return kVARhTDPowerFactorFlag;
    }

    private void setKVARhTDPowerFactorFlag(boolean kVARhTDPowerFactorFlag) {
        this.kVARhTDPowerFactorFlag = kVARhTDPowerFactorFlag;
    }

    public boolean isKVAhRMSPowerFactorFlag() {
        return kVAhRMSPowerFactorFlag;
    }

    private void setKVAhRMSPowerFactorFlag(boolean kVAhRMSPowerFactorFlag) {
        this.kVAhRMSPowerFactorFlag = kVAhRMSPowerFactorFlag;
    }

}
