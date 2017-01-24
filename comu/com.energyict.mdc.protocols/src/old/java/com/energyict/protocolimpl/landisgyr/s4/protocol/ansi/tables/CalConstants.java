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
public class CalConstants {

    private int singlePhaseMeterForm; // : UINT(0..2);
    // FILLER : FILL(3..7);

    /** Creates a new instance of RecordTemplate */
    public CalConstants(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setSinglePhaseMeterForm((data[offset]&0x07));
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ExpectedServiceType:\n");
        strBuff.append("   singlePhaseMeterForm="+getSinglePhaseMeterForm()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public int getSinglePhaseMeterForm() {
        return singlePhaseMeterForm;
    }

    public void setSinglePhaseMeterForm(int singlePhaseMeterForm) {
        this.singlePhaseMeterForm = singlePhaseMeterForm;
    }


}
