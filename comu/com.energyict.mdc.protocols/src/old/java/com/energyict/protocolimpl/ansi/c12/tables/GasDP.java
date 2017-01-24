/*
 * GasDp.java
 *
 * Created on 27 oktober 2005, 9:56
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
// differential pressure
public class GasDP {
    static private final int SIZE=2; // 2 x NiFormat
    private Number gasDpZero;
    private Number gasDpFullscale;

    /** Creates a new instance of GasDp */
    public GasDP(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setGasDpZero(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasDpFullscale(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getGasDpZero() {
        return gasDpZero;
    }

    public void setGasDpZero(Number gasDpZero) {
        this.gasDpZero = gasDpZero;
    }

    public Number getGasDpFullscale() {
        return gasDpFullscale;
    }

    public void setGasDpFullscale(Number gasDpFullscale) {
        this.gasDpFullscale = gasDpFullscale;
    }
}
