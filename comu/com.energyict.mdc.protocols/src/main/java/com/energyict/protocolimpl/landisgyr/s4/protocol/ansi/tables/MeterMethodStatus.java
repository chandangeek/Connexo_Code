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
public class MeterMethodStatus {

    private boolean demandOnly; // DEMAND_ONLY : BOOL(0);
    private boolean touCapable; //TOU_CAPABLE : BOOL(1);
    private boolean loadProfileEnabled; // LOAD_PROFILE_ENABLE : BOOL(2);
    private boolean bottomFeed; // BOTTOM_FEED : BOOL(3);
    //FILLER : FILL(4..7);

    /** Creates a new instance of RecordTemplate */
    public MeterMethodStatus(byte[] data, int offset, ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        int temp = (int)data[offset]&0xFF;
        setDemandOnly(((temp & 0x01) >> 0) == 0x01);
        setTouCapable(((temp & 0x02) >> 1) == 0x01);
        setLoadProfileEnabled(((temp & 0x04) >> 2) == 0x01);
        setBottomFeed(((temp & 0x08) >> 3) == 0x01);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("MeterMethodStatus:\n");
        strBuff.append("   bottomFeed="+isBottomFeed()+"\n");
        strBuff.append("   demandOnly="+isDemandOnly()+"\n");
        strBuff.append("   loadProfileEnabled="+isLoadProfileEnabled()+"\n");
        strBuff.append("   touCapable="+isTouCapable()+"\n");
        return strBuff.toString();
    }

    static public int getSize(ManufacturerTableFactory tableFactory) throws IOException {
        ConfigurationTable cfgt = tableFactory.getC12ProtocolLink().getStandardTableFactory().getConfigurationTable();
        return 1;
    }

    public boolean isDemandOnly() {
        return demandOnly;
    }

    private void setDemandOnly(boolean demandOnly) {
        this.demandOnly = demandOnly;
    }

    public boolean isTouCapable() {
        return touCapable;
    }

    private void setTouCapable(boolean touCapable) {
        this.touCapable = touCapable;
    }

    public boolean isLoadProfileEnabled() {
        return loadProfileEnabled;
    }

    private void setLoadProfileEnabled(boolean loadProfileEnabled) {
        this.loadProfileEnabled = loadProfileEnabled;
    }

    public boolean isBottomFeed() {
        return bottomFeed;
    }

    private void setBottomFeed(boolean bottomFeed) {
        this.bottomFeed = bottomFeed;
    }

}
