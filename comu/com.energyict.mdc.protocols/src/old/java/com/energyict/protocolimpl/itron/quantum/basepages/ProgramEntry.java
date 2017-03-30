/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * RecordTemplate.java
 *
 * Created on 13 september 2006, 13:34
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.itron.quantum.basepages;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ProgramEntry {

    private int registerType;
    private int registerNr;
    private int displaySetup;
    private int customerIdCode;

    /** Creates a new instance of RecordTemplate */
    public ProgramEntry(byte[] data, int offset) throws IOException {
        registerType = (int)data[offset++] & 0xff;
        registerNr = (int)data[offset++] & 0xff;
        displaySetup = (int)data[offset++] & 0xff;
        customerIdCode = (int)data[offset++] & 0xff;

    }

    public boolean isNonRegisterValue() {
        return ((getRegisterType() & 0x40) == 0x40);
    }

    public int getScale() {
       int scale = ((getDisplaySetup()>>3) & 0x03);
       if (scale == 0) return 3;
       else if (scale == 1) return 6;
       else return 0;
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ProgramEntry:\n");
        strBuff.append("   registerType=0x"+Integer.toHexString(getRegisterType())+"\n");
        try {
            if ((getRegisterType() & 0x40) == 0x40)
                strBuff.append("   registerNr="+getRegisterNr()+"(non register value)\n");
            else
                strBuff.append("   registerNr="+getRegisterNr()+"("+UnitTable.findUnitTable(getRegisterNr())+")\n");
        }
        catch(IOException e) {
            strBuff.append("   registerNr="+getRegisterNr()+"("+e.toString()+")\n");
        }
        strBuff.append("   displaySetup=0x"+Integer.toHexString(getDisplaySetup())+"\n");
        strBuff.append("   customerIdCode=0x"+Integer.toHexString(getCustomerIdCode())+"\n");
        return strBuff.toString();
    }


    static public int size() {
        return 4;
    }


    public int getRegisterType() {
        return registerType;
    }

    public void setRegisterType(int registerType) {
        this.registerType = registerType;
    }

    public int getRegisterNr() {
        return registerNr;
    }

    public void setRegisterNr(int registerNr) {
        this.registerNr = registerNr;
    }

    public int getDisplaySetup() {
        return displaySetup;
    }

    public void setDisplaySetup(int displaySetup) {
        this.displaySetup = displaySetup;
    }

    public int getCustomerIdCode() {
        return customerIdCode;
    }

    public void setCustomerIdCode(int customerIdCode) {
        this.customerIdCode = customerIdCode;
    }

}
