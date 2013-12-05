/*
 * GasTemp.java
 *
 * Created on 27 oktober 2005, 9:50
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
// temperature
public class GasTemp {

    static private final int SIZE=3; // 3 x NiFormat

    private Number gasTempZero;
    private Number gasTempFullscale;
    private Number baseTemperature;

    /** Creates a new instance of GasTemp */
    public GasTemp(byte[] data,int offset, int niFormat, int dataOrder) throws IOException {
        setGasTempZero(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setGasTempFullscale(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
        offset += C12ParseUtils.getNonIntegerSize(niFormat);
        setBaseTemperature(C12ParseUtils.getNumberFromNonInteger(data, offset, niFormat, dataOrder));
    }

    static public int getSize(int niFormat) throws IOException {
        return SIZE*C12ParseUtils.getNonIntegerSize(niFormat);
    }

    public Number getGasTempZero() {
        return gasTempZero;
    }

    public void setGasTempZero(Number gasTempZero) {
        this.gasTempZero = gasTempZero;
    }

    public Number getGasTempFullscale() {
        return gasTempFullscale;
    }

    public void setGasTempFullscale(Number gasTempFullscale) {
        this.gasTempFullscale = gasTempFullscale;
    }

    public Number getBaseTemperature() {
        return baseTemperature;
    }

    public void setBaseTemperature(Number baseTemperature) {
        this.baseTemperature = baseTemperature;
    }

}
