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
public class ServiceTypeEntry {

    private PhaseBitfield phaseCInfo; // : Phase_BFLD;
    private PhaseBitfield phaseBInfo; // : Phase_BFLD;
    private ServiceBitfield serviceConfigInfo; // : Service_BFLD;

    /** Creates a new instance of RecordTemplate */
    public ServiceTypeEntry(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setPhaseCInfo(new PhaseBitfield(data, offset, tableFactory));
        offset += PhaseBitfield.getSize(tableFactory);
        setPhaseBInfo(new PhaseBitfield(data, offset, tableFactory));
        offset += PhaseBitfield.getSize(tableFactory);
        setServiceConfigInfo(new ServiceBitfield(data, offset, tableFactory));
        offset += ServiceBitfield.getSize(tableFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ServiceTypeEntry:\n");
        strBuff.append("   phaseBInfo="+getPhaseBInfo()+"\n");
        strBuff.append("   phaseCInfo="+getPhaseCInfo()+"\n");
        strBuff.append("   serviceConfigInfo="+getServiceConfigInfo()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return PhaseBitfield.getSize(tableFactory)*2+ServiceBitfield.getSize(tableFactory);
    }

    public PhaseBitfield getPhaseCInfo() {
        return phaseCInfo;
    }

    public void setPhaseCInfo(PhaseBitfield phaseCInfo) {
        this.phaseCInfo = phaseCInfo;
    }

    public PhaseBitfield getPhaseBInfo() {
        return phaseBInfo;
    }

    public void setPhaseBInfo(PhaseBitfield phaseBInfo) {
        this.phaseBInfo = phaseBInfo;
    }

    public ServiceBitfield getServiceConfigInfo() {
        return serviceConfigInfo;
    }

    public void setServiceConfigInfo(ServiceBitfield serviceConfigInfo) {
        this.serviceConfigInfo = serviceConfigInfo;
    }

}
