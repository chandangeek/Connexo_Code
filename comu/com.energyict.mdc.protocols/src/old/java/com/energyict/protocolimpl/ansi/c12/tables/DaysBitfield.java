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
public class DaysBitfield {

    private boolean sunday;
    private boolean monday;
    private boolean tuesday;
    private boolean wednesday;
    private boolean thursday;
    private boolean friday;
    private boolean saturday;

    /** Creates a new instance of DaysBitfield */
    public DaysBitfield(byte[] data,int offset,TableFactory tableFactory) throws IOException {
        int temp = C12ParseUtils.getInt(data,offset++);
        sunday = (temp & 0x01) == 0x01;
        monday = (temp & 0x02) == 0x02;
        tuesday = (temp & 0x04) == 0x04;
        wednesday = (temp & 0x08) == 0x08;
        thursday = (temp & 0x10) == 0x10;
        friday = (temp & 0x20) == 0x20;
        saturday = (temp & 0x40) == 0x40;

    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("DaysBitfield:\n");
        strBuff.append("   friday="+isFriday()+"\n");
        strBuff.append("   monday="+isMonday()+"\n");
        strBuff.append("   saturday="+isSaturday()+"\n");
        strBuff.append("   sunday="+isSunday()+"\n");
        strBuff.append("   thursday="+isThursday()+"\n");
        strBuff.append("   tuesday="+isTuesday()+"\n");
        strBuff.append("   wednesday="+isWednesday()+"\n");
        return strBuff.toString();
    }

    static public int getSize(TableFactory tableFactory) throws IOException {
        return 1;
    }

    public boolean isSunday() {
        return sunday;
    }

    public boolean isMonday() {
        return monday;
    }

    public boolean isTuesday() {
        return tuesday;
    }

    public boolean isWednesday() {
        return wednesday;
    }

    public boolean isThursday() {
        return thursday;
    }

    public boolean isFriday() {
        return friday;
    }

    public boolean isSaturday() {
        return saturday;
    }

}
