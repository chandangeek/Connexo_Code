/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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
public class S4ConfigMask {

    private boolean aXLRegisterConfiguration; // AXL_REGISTER_CONFIGURATION : BOOL(0);
    private boolean aXRegisterConfiguration; // AX_REGISTER_CONFIGURATION : BOOL(1);
    private boolean singlePhaseServiceExpected; // SINGLE_PHASE_SERVICE_EXPECTED : BOOL(2);
    private boolean loadProfileCapable; // LOAD_PROFILE_CAPABLE : BOOL(3);
    private boolean tlcCapable; //TLC_CAPABLE :BOOL(4);
    //RESERVED : BOOL(5);
    private boolean rAM128KPresent; // 128K_RAM_ PRESENT : BOOL(6);
    private boolean rAM512KPresent; // 512K_RAM_ PRESENT : BOOL(7);

    /** Creates a new instance of RecordTemplate */
    public S4ConfigMask(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int temp = (int)data[offset] & 0xFF;
        setAXLRegisterConfiguration((((temp>>0)&0x01)==0x01));
        setAXRegisterConfiguration((((temp>>1)&0x01)==0x01));
        setSinglePhaseServiceExpected((((temp>>2)&0x01)==0x01));
        setLoadProfileCapable((((temp>>3)&0x01)==0x01));
        setTlcCapable((((temp>>4)&0x01)==0x01));
        setRAM128KPresent((((temp>>6)&0x01)==0x01));
        setRAM512KPresent((((temp>>7)&0x01)==0x01));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("S4ConfigMask:\n");
        strBuff.append("   AXLRegisterConfiguration="+isAXLRegisterConfiguration()+"\n");
        strBuff.append("   AXRegisterConfiguration="+isAXRegisterConfiguration()+"\n");
        strBuff.append("   RAM128KPresent="+isRAM128KPresent()+"\n");
        strBuff.append("   RAM512KPresent="+isRAM512KPresent()+"\n");
        strBuff.append("   loadProfileCapable="+isLoadProfileCapable()+"\n");
        strBuff.append("   singlePhaseServiceExpected="+isSinglePhaseServiceExpected()+"\n");
        strBuff.append("   tlcCapable="+isTlcCapable()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public boolean isAXLRegisterConfiguration() {
        return aXLRegisterConfiguration;
    }

    public void setAXLRegisterConfiguration(boolean aXLRegisterConfiguration) {
        this.aXLRegisterConfiguration = aXLRegisterConfiguration;
    }

    public boolean isAXRegisterConfiguration() {
        return aXRegisterConfiguration;
    }

    public void setAXRegisterConfiguration(boolean aXRegisterConfiguration) {
        this.aXRegisterConfiguration = aXRegisterConfiguration;
    }

    public boolean isSinglePhaseServiceExpected() {
        return singlePhaseServiceExpected;
    }

    public void setSinglePhaseServiceExpected(boolean singlePhaseServiceExpected) {
        this.singlePhaseServiceExpected = singlePhaseServiceExpected;
    }

    public boolean isLoadProfileCapable() {
        return loadProfileCapable;
    }

    public void setLoadProfileCapable(boolean loadProfileCapable) {
        this.loadProfileCapable = loadProfileCapable;
    }

    public boolean isTlcCapable() {
        return tlcCapable;
    }

    public void setTlcCapable(boolean tlcCapable) {
        this.tlcCapable = tlcCapable;
    }

    public boolean isRAM128KPresent() {
        return rAM128KPresent;
    }

    public void setRAM128KPresent(boolean rAM128KPresent) {
        this.rAM128KPresent = rAM128KPresent;
    }

    public boolean isRAM512KPresent() {
        return rAM512KPresent;
    }

    public void setRAM512KPresent(boolean rAM512KPresent) {
        this.rAM512KPresent = rAM512KPresent;
    }

}
