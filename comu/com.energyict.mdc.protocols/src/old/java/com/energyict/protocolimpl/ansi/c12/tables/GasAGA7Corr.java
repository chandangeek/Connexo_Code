/*
 * GasAGA7Corr.java
 *
 * Created on 27 oktober 2005, 10:16
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
public class GasAGA7Corr {

    private GasPress gasPressParm;
    private GasTemp gasTempParm;
    private Number auxCorrFactor;
    private Number gasAGA7Corr;

    /** Creates a new instance of GasAGA7Corr */
    public GasAGA7Corr(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setGasPressParm(new GasPress(data,offset,niFormat,dataOrder));
        offset += GasPress.getSize(niFormat);
        setGasTempParm(new GasTemp(data,offset,niFormat,dataOrder));
        offset += GasTemp.getSize(niFormat);
        setAuxCorrFactor(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasAGA7Corr(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return C12ParseUtils.getNonIntegerSize(niFormat)*2+GasPress.getSize(niFormat)+GasTemp.getSize(niFormat);
    }

    public GasPress getGasPressParm() {
        return gasPressParm;
    }

    public void setGasPressParm(GasPress gasPressParm) {
        this.gasPressParm = gasPressParm;
    }

    public GasTemp getGasTempParm() {
        return gasTempParm;
    }

    public void setGasTempParm(GasTemp gasTempParm) {
        this.gasTempParm = gasTempParm;
    }

    public Number getAuxCorrFactor() {
        return auxCorrFactor;
    }

    public void setAuxCorrFactor(Number auxCorrFactor) {
        this.auxCorrFactor = auxCorrFactor;
    }

    public Number getGasAGA7Corr() {
        return gasAGA7Corr;
    }

    public void setGasAGA7Corr(Number gasAGA7Corr) {
        this.gasAGA7Corr = gasAGA7Corr;
    }

}
