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
public class DemandResetConfig {

    private boolean demandResetType; // : BOOL(0);
    private boolean overloadErrorClear; // : BOOL(1);
    // FILLER : FILL(2..7);

    /** Creates a new instance of RecordTemplate */
    public DemandResetConfig(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int temp = (int)data[offset++]&0xFF;
        setDemandResetType(((temp & 0x01) == 0x01));
        setOverloadErrorClear((((temp & 0x02)>>1) == 0x01));
    }


    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DemandResetConfig:\n");
        strBuff.append("   demandResetType="+isDemandResetType()+"\n");
        strBuff.append("   overloadErrorClear="+isOverloadErrorClear()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public boolean isDemandResetType() {
        return demandResetType;
    }

    private void setDemandResetType(boolean demandResetType) {
        this.demandResetType = demandResetType;
    }

    public boolean isOverloadErrorClear() {
        return overloadErrorClear;
    }

    private void setOverloadErrorClear(boolean overloadErrorClear) {
        this.overloadErrorClear = overloadErrorClear;
    }


}
