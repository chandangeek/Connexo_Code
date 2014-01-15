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

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class VerificationLedConfig {

    private int verificationLEDCntrl; // : UINT8;
    private String verificationLEDDisplay; // : ARRAY[6] OF CHAR;

    /** Creates a new instance of RecordTemplate */
    public VerificationLedConfig(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setVerificationLEDCntrl((int)data[offset++]&0xff);
        setVerificationLEDDisplay(new String(ProtocolUtils.getSubArray2(data, offset, 6)));
        offset+=6;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("VerificationLedConfig:\n");
        strBuff.append("   verificationLEDCntrl="+getVerificationLEDCntrl()+"\n");
        strBuff.append("   verificationLEDDisplay="+getVerificationLEDDisplay()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 7;
    }

    public int getVerificationLEDCntrl() {
        return verificationLEDCntrl;
    }

    private void setVerificationLEDCntrl(int verificationLEDCntrl) {
        this.verificationLEDCntrl = verificationLEDCntrl;
    }

    public String getVerificationLEDDisplay() {
        return verificationLEDDisplay;
    }

    private void setVerificationLEDDisplay(String verificationLEDDisplay) {
        this.verificationLEDDisplay = verificationLEDDisplay;
    }

}
