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

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;
import com.energyict.protocolimpl.ansi.c12.tables.ConfigurationTable;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class DSPFirmwareRevision {


    private int dspMinorRevision; // DSP_MINOR_REVISION : ARRAY[1] OF BCD;
    private int dspMajorRevision; // DSP_MAJOR_REVISION : ARRAY[1] OF BCD;

    /** Creates a new instance of RecordTemplate */
    public DSPFirmwareRevision(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        setDspMinorRevision((int)C12ParseUtils.getBCD2Long(data, offset++, 1, cfgt.getDataOrder()));
        setDspMajorRevision((int)C12ParseUtils.getBCD2Long(data, offset++, 1, cfgt.getDataOrder()));

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DSPFirmwareRevision:\n");
        strBuff.append("   dspMajorRevision="+getDspMajorRevision()+"\n");
        strBuff.append("   dspMinorRevision="+getDspMinorRevision()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 2;
    }

    public int getDspMinorRevision() {
        return dspMinorRevision;
    }

    public void setDspMinorRevision(int dspMinorRevision) {
        this.dspMinorRevision = dspMinorRevision;
    }

    public int getDspMajorRevision() {
        return dspMajorRevision;
    }

    public void setDspMajorRevision(int dspMajorRevision) {
        this.dspMajorRevision = dspMajorRevision;
    }

}
