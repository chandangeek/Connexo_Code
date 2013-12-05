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
public class ServiceBitfield {

    private int phaseAVoltage; // : UINT(0..3);
    private boolean rotationFlag; // : BOOL(4);
    private int nominalVoltageCode; // : UINT(5..6);
    private boolean serviceConfigBit; // : BOOL(7);

    /** Creates a new instance of RecordTemplate */
    public ServiceBitfield(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setPhaseAVoltage(data[offset] & 0x0F);
        setRotationFlag(((data[offset] >> 4) & 0x01) == 0x01);
        setNominalVoltageCode((data[offset] >> 5) & 0x03);
        setServiceConfigBit(((data[offset] >> 7) & 0x01) == 0x01);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ServiceBitfield:\n");
        strBuff.append("   nominalVoltageCode="+getNominalVoltageCode()+"\n");
        strBuff.append("   phaseAVoltage="+getPhaseAVoltage()+"\n");
        strBuff.append("   rotationFlag="+isRotationFlag()+"\n");
        strBuff.append("   serviceConfigBit="+isServiceConfigBit()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public int getPhaseAVoltage() {
        return phaseAVoltage;
    }

    public void setPhaseAVoltage(int phaseAVoltage) {
        this.phaseAVoltage = phaseAVoltage;
    }

    public boolean isRotationFlag() {
        return rotationFlag;
    }

    public void setRotationFlag(boolean rotationFlag) {
        this.rotationFlag = rotationFlag;
    }

    public int getNominalVoltageCode() {
        return nominalVoltageCode;
    }

    public void setNominalVoltageCode(int nominalVoltageCode) {
        this.nominalVoltageCode = nominalVoltageCode;
    }

    public boolean isServiceConfigBit() {
        return serviceConfigBit;
    }

    public void setServiceConfigBit(boolean serviceConfigBit) {
        this.serviceConfigBit = serviceConfigBit;
    }

}
