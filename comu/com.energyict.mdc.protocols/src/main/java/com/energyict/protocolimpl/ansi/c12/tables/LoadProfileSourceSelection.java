/*
 * LoadProfileSourceSelection.java
 *
 * Created on 7 november 2005, 17:13
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;

import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LoadProfileSourceSelection {

    private int channelFlag; // 8 bit
    private boolean endReadingFlag; // bit 0, if true, the channel does have an associated end reading

    private int loadProfileSourceSelect; // 8 bit index in the source definition table describing the source of the interval data for a specific channel
    private int endTimeBlockReadingSourceSelect; // 8 bit index in the source definition table describing the source of the block end time reading data for a specific channel

    /** Creates a new instance of LoadProfileSourceSelection */
    public LoadProfileSourceSelection(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        setChannelFlag(C12ParseUtils.getInt(data,offset++));
        endReadingFlag = (getChannelFlag()&0x01) == 0x01;
        setLoadProfileSourceSelect(C12ParseUtils.getInt(data,offset++));
        setEndTimeBlockReadingSourceSelect(C12ParseUtils.getInt(data,offset++));
    }
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("LoadProfileSourceSelection: channelFlag=0x"+Integer.toHexString(getChannelFlag())+", loadProfileSourceSelect="+getLoadProfileSourceSelect()+", endTimeBlockReadingSourceSelect="+getEndTimeBlockReadingSourceSelect()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 3;
    }

    public int getChannelFlag() {
        return channelFlag;
    }

    public void setChannelFlag(int channelFlag) {
        this.channelFlag = channelFlag;
    }

    public boolean isEndReadingFlag() {
        return endReadingFlag;
    }

    public void setEndReadingFlag(boolean endReadingFlag) {
        this.endReadingFlag = endReadingFlag;
    }

    public int getLoadProfileSourceSelect() {
        return loadProfileSourceSelect;
    }

    public void setLoadProfileSourceSelect(int loadProfileSourceSelect) {
        this.loadProfileSourceSelect = loadProfileSourceSelect;
    }

    public int getEndTimeBlockReadingSourceSelect() {
        return endTimeBlockReadingSourceSelect;
    }

    public void setEndTimeBlockReadingSourceSelect(int endTimeBlockReadingSourceSelect) {
        this.endTimeBlockReadingSourceSelect = endTimeBlockReadingSourceSelect;
    }
}
