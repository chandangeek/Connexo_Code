/*
 * ElectricConstants.java
 *
 * Created on 27 oktober 2005, 10:47
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
public class ElectricConstants extends AbstractConstants {

    private Number multiplier;
    private Number offset;
    private SetApplied set1Constants;
    private SetApplied set2Constants;

    /** Creates a new instance of ElectricConstants */
    public ElectricConstants(byte[] data,int offs, int niFormat,ActualSourcesLimitingTable aslt, int dataFormat) throws IOException {
        setMultiplier(C12ParseUtils.getNumberFromNonInteger(data, offs, niFormat, dataFormat));
        offs += C12ParseUtils.getNonIntegerSize(niFormat);
        setOffset(C12ParseUtils.getNumberFromNonInteger(data, offs, niFormat, dataFormat));
        offs += C12ParseUtils.getNonIntegerSize(niFormat);
        if (aslt.isSet1Present()) {
            set1Constants = new SetApplied(data,offs, niFormat, dataFormat);
            offs += SetApplied.getSize(niFormat);
        }
        if (aslt.isSet2Present())
           set2Constants = new SetApplied(data,offs, niFormat, dataFormat);
    }

    static public int getSize(int niFormat,ActualSourcesLimitingTable aslt) throws IOException {
        return C12ParseUtils.getNonIntegerSize(niFormat)*2+(aslt.isSet1Present()?SetApplied.getSize(niFormat):0)+(aslt.isSet2Present()?SetApplied.getSize(niFormat):0);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ElectricConstants: \n");
        strBuff.append("    multiplier="+getMultiplier()+", offset="+getOffset()+"\n");
        strBuff.append("    set1Constants="+getSet1Constants()+"\n");
        strBuff.append("    set2Constants="+getSet2Constants()+"\n");
        return strBuff.toString();
    }

    protected int getConstantsType() {
        return CONSTANTS_ELECTRIC;
    }

    public Number getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(Number multiplier) {
        this.multiplier = multiplier;
    }

    public Number getOffset() {
        return offset;
    }

    public void setOffset(Number offset) {
        this.offset = offset;
    }

    public SetApplied getSet1Constants() {
        return set1Constants;
    }

    public void setSet1Constants(SetApplied set1Constants) {
        this.set1Constants = set1Constants;
    }

    public SetApplied getSet2Constants() {
        return set2Constants;
    }

    public void setSet2Constants(SetApplied set2Constants) {
        this.set2Constants = set2Constants;
    }

}
