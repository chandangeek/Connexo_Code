/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SetApplied.java
 *
 * Created on 27 oktober 2005, 10:49
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
public class SetApplied {

    private int setFlagsBitfield; // 8 bit
    private Number ratioF1;
    private Number ratioP1;

    /** Creates a new instance of SetApplied */
    public SetApplied(byte[] data,int offset, int niFormat, int dataFormat) throws IOException {
        setSetFlagsBitfield(C12ParseUtils.getInt(data,offset));
        offset++;
        setRatioF1(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat,  dataFormat));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setRatioP1(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataFormat));
    }

    public String toString() {
        return "SetApplied: setFlagsBitfield=0x"+Integer.toHexString(getSetFlagsBitfield())+", ratioF1="+getRatioF1()+", ratioP1="+getRatioP1();
    }

    public boolean isSetAppliedBit() {
        return (setFlagsBitfield & 0x01) == 0x01;
    }

    static public int getSize(int niFormat) throws IOException {
        return C12ParseUtils.getNonIntegerSize(niFormat)*2+1;
    }

    public int getSetFlagsBitfield() {
        return setFlagsBitfield;
    }

    public void setSetFlagsBitfield(int setFlagsBitfield) {
        this.setFlagsBitfield = setFlagsBitfield;
    }

    public Number getRatioF1() {
        return ratioF1;
    }

    public void setRatioF1(Number ratioF1) {
        this.ratioF1 = ratioF1;
    }

    public Number getRatioP1() {
        return ratioP1;
    }

    public void setRatioP1(Number ratioP1) {
        this.ratioP1 = ratioP1;
    }

}
