/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * MeterDataReadout.java
 *
 * Created on 31 oktober 2003, 11:04
 */

package com.energyict.protocolcommon;

import com.energyict.mdc.upl.io.NestedIOException;

import java.io.IOException;

/**
 * @author Koen
 */
public class MeterDataReadout {

    byte[] dataReadout;
    String strDataReadout;

    /**
     * Creates a new instance of MeterDataReadout
     */
    public MeterDataReadout(byte[] dataReadout) {
        this.dataReadout = dataReadout;
        this.strDataReadout = dataReadout != null ? new String(dataReadout) : "";
    }

    public String getValue(String register) throws IOException {
        return findRegister(register);
    }

    public String getValue(String[] registers) throws IOException {
        IOException ex = null;
        for (int i = 0; i < registers.length; i++) {
            try {
                return findRegister(registers[i]);
            } catch (IOException e) {
                ex = e;
            }
        }
        throw new NestedIOException(ex);
    }

    private String findRegister(String register) throws IOException {
        String substr;
        int index;
        index = strDataReadout.indexOf(register + "(");
        if (index == -1) {
            throw new IOException("MeterDataReadout, register " + register + " not found in datadump!");
        }
        substr = strDataReadout.substring(index);
        index = substr.indexOf(")");
        if (index == -1) {
            throw new IOException("MeterDataReadout, invalid datadump, ')' not found!");
        }
        return substr.substring(substr.indexOf("(") + 1, index);
    }

    /**
     * Getter for property dataReadout.
     *
     * @return Value of property dataReadout.
     */
    public byte[] getDataReadout() {
        return this.dataReadout;
    }
}
