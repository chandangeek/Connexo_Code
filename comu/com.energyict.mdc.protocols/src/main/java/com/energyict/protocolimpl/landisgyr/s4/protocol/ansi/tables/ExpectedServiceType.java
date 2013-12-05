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
public class ExpectedServiceType {

    private int singlePhaseServiceType; // : UINT(0..1);
    // FILLER : FILL(2..7);

    /** Creates a new instance of RecordTemplate */
    public ExpectedServiceType(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setSinglePhaseServiceType(data[offset]&0x03);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ExpectedServiceType:\n");
        strBuff.append("   singlePhaseServiceType="+getSinglePhaseServiceType()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public int getSinglePhaseServiceType() {
        return singlePhaseServiceType;
    }

    public void setSinglePhaseServiceType(int singlePhaseServiceType) {
        this.singlePhaseServiceType = singlePhaseServiceType;
    }

}
