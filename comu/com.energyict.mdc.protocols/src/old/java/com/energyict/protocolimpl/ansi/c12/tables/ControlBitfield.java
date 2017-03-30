/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DaysBitfield.java
 *
 * Created on 23 februari 2006, 14:47
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.energyict.protocolimpl.ansi.c12.tables;


import com.energyict.protocolimpl.ansi.c12.C12ParseUtils;

import java.io.IOException;
/**
 *
 * @author koen
 */
public class ControlBitfield {

    private int primaryPhoneNumber; // bit 2..0
    // reserved bit 3
    private int secondaryPhoneNumber; // bit 6..4
    private boolean useWindows; // bit 7


    /** Creates a new instance of DaysBitfield */
    public ControlBitfield(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int temp;
        temp = C12ParseUtils.getInt(data,offset++);
        primaryPhoneNumber = temp & 0x07;
        secondaryPhoneNumber = (temp & 0x70) >> 4;
        useWindows = (temp & 0x80) == 0x80;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DaysBitfield:\n");
        strBuff.append("   primaryPhoneNumber="+getPrimaryPhoneNumber()+"\n");
        strBuff.append("   secondaryPhoneNumber="+getSecondaryPhoneNumber()+"\n");
        strBuff.append("   useWindows="+isUseWindows()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

    public int getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public int getSecondaryPhoneNumber() {
        return secondaryPhoneNumber;
    }

    public boolean isUseWindows() {
        return useWindows;
    }

}
