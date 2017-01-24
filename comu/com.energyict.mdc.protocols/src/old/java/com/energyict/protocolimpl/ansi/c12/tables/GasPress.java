/*
 * GasPress.java
 *
 * Created on 27 oktober 2005, 9:43
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
// pressure
public class GasPress {

    static private final int SIZE=3; // 3 x NiFormat

    private Number gasPressZero;
    private Number gasPressFullscale;
    private Number basePressure;

    /** Creates a new instance of GasPress */
    public GasPress(byte[] data, int offset, int niFormat, int dataOrder) throws IOException {
        setGasPressZero(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasPressFullscale(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setBasePressure(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getGasPressZero() {
        return gasPressZero;
    }

    public void setGasPressZero(Number gasPressZero) {
        this.gasPressZero = gasPressZero;
    }

    public Number getGasPressFullscale() {
        return gasPressFullscale;
    }

    public void setGasPressFullscale(Number gasPressFullscale) {
        this.gasPressFullscale = gasPressFullscale;
    }

    public Number getBasePressure() {
        return basePressure;
    }

    public void setBasePressure(Number basePressure) {
        this.basePressure = basePressure;
    }

}
