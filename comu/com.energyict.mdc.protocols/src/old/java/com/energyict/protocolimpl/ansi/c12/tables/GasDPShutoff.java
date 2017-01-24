/*
 * GasDPShutoff.java
 *
 * Created on 27 oktober 2005, 10:24
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
public class GasDPShutoff {

    static private final int SIZE=1; // 1 x NiFormat
    private Number gasShutoff;

    /** Creates a new instance of GasDPShutoff */
    public GasDPShutoff(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setGasShutoff(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getGasShutoff() {
        return gasShutoff;
    }

    public void setGasShutoff(Number gasShutoff) {
        this.gasShutoff = gasShutoff;
    }

}
